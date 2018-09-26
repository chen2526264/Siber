const {app, BrowserWindow, Menu, MenuItem, dialog} = require('electron');
const path = require('path');
const url = require('url');
const fs = require('fs');
// const http = require('http');
// const https = require('https');
const request = require('request');
const os = require('os');
const childProcess = require('child_process');
//解压zip包工具
const DecompressZip = require('decompress-zip');
const mkdirp = require('mkdirp');
const logger = require('electron-log');
logger.transports.file.format = '{h}:{i}:{s}:{ms} {text}';
logger.transports.file.maxSize = 5 * 1024 * 1024;
logger.transports.file.level = 'info';

const SIBER_DATA_DIR = getUserHome() + '/siber_data';
const UPLOAD_FILE_DIR = SIBER_DATA_DIR + '/update_files';
const APP_FILE_NAME = "app.zip";

var currentVersion = app.getVersion();
var newVersion;

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;
let jarProcess;

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', function () {
    start();
});

// Quit when all windows are closed.
app.on('window-all-closed', function () {
    logger.info("window-all-closed");
    // On OS X it is common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
        app.quit()
    }
});

app.on('activate', function () {
    // On OS X it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (mainWindow === null) {
        start();
    }
});


//启动，执行所有初始化步骤
function start() {
    logger.info("start begin!");
    createWindow();

    checkUpdate();

    startJar();

    loadIndexPage();

    // Emitted when the window is closed.
    mainWindow.on('closed', shutdown);

    // Create the Application's main menu
    createMainMenu();
}


function checkUpdate() {
    logger.info("checkUpdate begin!");

    logger.info("currentVersion is " + currentVersion);
    // let newVersion;
    let options = {
        url: 'https://api.github.com/repos/Sibergo/Siber/releases/latest',
        headers: {
            'User-Agent': 'request'
            // ,"Authorization": "token c21ae74e62a8f9a1b6b044749a2ac72ab65a437f"
        }
    };

    request(options, function (error, response, body) {
        logger.info('error:', error);
        logger.info("response.statusCode id ", response && response.statusCode);
        if (error) {
            logger.info("request error," + error);
            return;
        }
        if (!error && response.statusCode === 200) {
            let releaseInfo = JSON.parse(body);
            //新版本号
            newVersion = releaseInfo.tag_name;
            //新版本的名字
            let name = releaseInfo.name;
            //如果新版本小于最新版本，且新版本名字是以"siber-"开头，说明是正式版本,下载更新
            if (currentVersion < newVersion && name.startsWith('siber-')) {
                updateIfFileAlreadyExist(releaseInfo, 0);
            } else {
                logger.info("当前已是最新版本！");
            }
        } else {
            logger.info("检查更新包失败！");
        }
    });
};


/**
 * 如果需要更新的包已经存在于本地，且通过文件大小的校验，则直接解压,否则下载更新包
 * @param assets
 * @param newVersion
 */
function updateIfFileAlreadyExist(releaseInfo, count) {
    logger.info("updateIfFileAlreadyExist begin!");
    if (count == 5) {
        return;
    }
    //获得需要下载的更新包的元数据信息
    let appAsset;
    for (let i = 0; i < releaseInfo.assets.length; i++) {
        if (releaseInfo.assets[i].name === APP_FILE_NAME) {
            appAsset = releaseInfo.assets[i];
            break;
        }
    }
    //检查包是否存在
    let updateFilePath = UPLOAD_FILE_DIR + "/" + newVersion + "/" + APP_FILE_NAME;

    let exist = fs.existsSync(updateFilePath);
    //如果存在，提示用户，如果用户同意，就更新。
    if (exist && fs.statSync(updateFilePath).size == appAsset.size) {
        promptUserAndUpdate(releaseInfo, updateFilePath);
    } else {  //  如果不存在，则下载
        downloadUpdateFile(releaseInfo, appAsset, count);
    }
}

function promptUserAndUpdate(releaseInfo, updateFilePath) {
    logger.info("promptUserAndUpdate begin!");

    let options = {
        // title: "更新",
        message: "检查到新版本可用，是否更新？",
        detail: releaseInfo.body,
        buttons: ['否', '是']
    };

    dialog.showMessageBox(options, function (response) {
        logger.info("press button is " + response);
        //如果用户点击"是"，则解压并重启
        if (response == 1) {
            unzipUpdateFileAndRestart(updateFilePath);
        }
    });

}

function downloadUpdateFile(releaseInfo, appAsset, count) {
    logger.info("downloadUpdateFile begin!");

    //下载的url
    let fileUrlToBeDownload = appAsset.browser_download_url;

    //下载的包存放的目录,如果目录不存在则创建
    let fileParentDir = UPLOAD_FILE_DIR + "/" + newVersion;
    mkdirp.sync(fileParentDir);

    //下载的包的完整路径
    let filePath = fileParentDir + "/" + APP_FILE_NAME;

    logger.info(filePath + ' 开始下载');
    downloadFile(fileUrlToBeDownload, filePath, function () {
        logger.info(filePath + ' 下载完毕');
        updateIfFileAlreadyExist(releaseInfo, count + 1);
    });
}

function downloadFile(uri, filename, callback) {
    let stream = fs.createWriteStream(filename);
    request(uri)
        .on('error', function (err) {
            logger.error('File download failed', err);
        })
        .pipe(stream)
        .on('close', callback);
}

function getUserHome() {
    let userHome = os.homedir();
    logger.info("User Home dir is " + userHome);
    return userHome;
}

function loadIndexPage() {
    logger.info("enter loadIndexPage");
    //延迟加载index.html，等待java进程启动好。
    setTimeout(function () {
        logger.info("setTimeout done");
        mainWindow.loadURL(url.format({
            pathname: path.join(__dirname, 'build/index.html'),
            protocol: 'file:',
            slashes: true
        }));
    }, 5000);
}

function shutdown() {
    // Dereference the window object, usually you would store windows
    // in an array if your app supports multi windows, this is the time
    // when you should delete the corresponding element.
    logger.info('enter shutdown!!');
    //销毁java进程
    if (jarProcess != null) {
        jarProcess.kill('SIGINT');
    }
    mainWindow = null;
};

function createWindow() {
    logger.info("enter createWindow");
    // Create the browser window.
    mainWindow = new BrowserWindow({width: 960, height: 800, minWidth: 960, minHeight: 800});

    // Open the DevTools.
    // mainWindow.webContents.openDevTools();
};


function startJar() {
    logger.info("enter startJar");
    let jarPath = __dirname + "/lib/tool.jar";
    let args = ['-jar', jarPath];
    jarProcess = childProcess.spawn('java', args);

    //需要接收子进程的输出，不然当子进程的输出大于buffer时，子进程会阻塞。
    jarProcess.stdout.on('data', (data) => {
        // console.log(`stdout: ${data}`);
    });

    jarProcess.stderr.on('data', (data) => {
        logger.info(`stderr: ${data}`);
    });

    jarProcess.on('close', (code) => {
        logger.info(`child process exited with code ${code}`);
    });
}


function unzipUpdateFileAndRestart(updateFilePath) {
    // var ZIP_FILE_PATH = "/Users/chenjiong/A_Development/A_Repositorys/S_Siber/client/dist/app.zip";
    //解压目录，就是该应用的app目录
    var destinationPath = __dirname;
    var unZipper = new DecompressZip(updateFilePath);

    // Add the error event listener
    unZipper.on('error', function (err) {
        logger.info('Caught an error', err);
    });

    // Notify when everything is extracted
    unZipper.on('extract', function (log) {
        // console.logger('Finished extracting');
        // console.logger('delete update_files dir');
        deleteFolder(UPLOAD_FILE_DIR);
        //重启electron
        restartElectron();
    });

    // Notify "progress" of the decompressed files
    unZipper.on('progress', function (fileIndex, fileCount) {
        logger.info('Extracted file ' + (fileIndex + 1) + ' of ' + fileCount);
    });

    fs.stat(updateFilePath, function (err, stat) {
        if (stat && stat.isFile()) {
            logger.info('文件存在');
            // Start extraction of the content
            unZipper.extract({
                path: destinationPath
            });
        } else {
            logger.info('文件不存在或不是标准文件');
        }
    });
}

function deleteFolder(path) {
    logger.info("enter deleteFolder, delete path is " + path);
    var files = [];
    if (fs.existsSync(path)) {
        files = fs.readdirSync(path);
        files.forEach(function (file, index) {
            var curPath = path + "/" + file;
            if (fs.statSync(curPath).isDirectory()) { // recurse
                deleteFolder(curPath);
            } else { // delete file
                console.log("begin delete file " + curPath);
                fs.unlinkSync(curPath);
            }
        });
        fs.rmdirSync(path);
    }
}

function restartElectron() {
    logger.info("restart electron");
    var exec = childProcess.exec;
    exec(process.argv.join(' ')); // execute the command that was used to run the app
    app.quit() // quit the current app
}


function createMainMenu() {

    let template2 = [{
        label: 'Electron',
        submenu: [
            {
                label: 'About Siber',
                selector: 'orderFrontStandardAboutPanel:'
            },
            {
                type: 'separator'
            },
            {
                label: 'Services',
                submenu: []
            },
            {
                type: 'separator'
            },
            {
                label: 'Hide Siber',
                accelerator: 'Command+H',
                selector: 'hide:'
            },
            {
                label: 'Hide Others',
                accelerator: 'Command+Shift+H',
                selector: 'hideOtherApplications:'
            },
            {
                label: 'Show All',
                selector: 'unhideAllApplications:'
            },
            {
                type: 'separator'
            },
            {
                label: 'Quit',
                accelerator: 'Command+Q',
                click: function () {
                    app.quit();
                }
            },
        ]
    },
        {
            label: 'Edit',
            submenu: [
                {
                    label: 'Undo',
                    accelerator: 'Command+Z',
                    selector: 'undo:'
                },
                {
                    label: 'Redo',
                    accelerator: 'Shift+Command+Z',
                    selector: 'redo:'
                },
                {
                    type: 'separator'
                },
                {
                    label: 'Cut',
                    accelerator: 'Command+X',
                    selector: 'cut:'
                },
                {
                    label: 'Copy',
                    accelerator: 'Command+C',
                    selector: 'copy:'
                },
                {
                    label: 'Paste',
                    accelerator: 'Command+V',
                    selector: 'paste:'
                },
                {
                    label: 'Select All',
                    accelerator: 'Command+A',
                    selector: 'selectAll:'
                },
            ]
        },
        // {
        //     label: 'View',
        //     submenu: [
        //         {
        //             label: 'Reload',
        //             accelerator: 'Command+R',
        //             click: function() { BrowserWindow.getFocusedWindow().reloadIgnoringCache(); }
        //         },
        //         {
        //             label: 'Toggle DevTools',
        //             accelerator: 'Alt+Command+I',
        //             click: function() { BrowserWindow.getFocusedWindow().toggleDevTools(); }
        //         },
        //     ]
        // },
        {
            label: 'Window',
            submenu: [
                {
                    label: 'Minimize',
                    accelerator: 'Command+M',
                    selector: 'performMiniaturize:'
                },
                {
                    label: 'Close',
                    accelerator: 'Command+W',
                    selector: 'performClose:'
                },
                {
                    type: 'separator'
                },
                {
                    label: 'Bring All to Front',
                    selector: 'arrangeInFront:'
                },
            ]
        },
        {
            label: 'Help',
            submenu: []
        }];

    const template = [
        {
            label: 'Edit',
            submenu: [
                {
                    role: 'undo'
                },
                {
                    role: 'redo'
                },
                {
                    type: 'separator'
                },
                {
                    role: 'cut'
                },
                {
                    role: 'copy'
                },
                {
                    role: 'paste'
                },
                {
                    role: 'pasteandmatchstyle'
                },
                {
                    role: 'delete'
                },
                {
                    role: 'selectall'
                }
            ]
        },
        {
            label: 'View',
            submenu: [
                {
                    label: 'Reload',
                    accelerator: 'CmdOrCtrl+R',
                    click(item, focusedWindow) {
                        if (focusedWindow) focusedWindow.reload()
                    }
                },
                {
                    label: 'Toggle Developer Tools',
                    accelerator: process.platform === 'darwin' ? 'Alt+Command+I' : 'Ctrl+Shift+I',
                    click(item, focusedWindow) {
                        if (focusedWindow) focusedWindow.webContents.toggleDevTools()
                    }
                },
                {
                    type: 'separator'
                },
                {
                    role: 'resetzoom'
                },
                {
                    role: 'zoomin'
                },
                {
                    role: 'zoomout'
                },
                {
                    type: 'separator'
                },
                {
                    role: 'togglefullscreen'
                }
            ]
        },
        {
            role: 'window',
            submenu: [
                {
                    role: 'minimize'
                },
                {
                    role: 'close'
                }
            ]
        },
        {
            role: 'help',
            submenu: [
                {
                    label: 'Learn More',
                    click() {
                        require('electron').shell.openExternal('http://electron.atom.io')
                    }
                }
            ]
        }
    ]

    if (process.platform === 'darwin') {
        const name = app.getName()
        template.unshift({
            label: name,
            submenu: [
                {
                    // role: 'about'
                    label: 'about siber',
                    click(item, focusedWindow) {
                        let options = {
                            type: "info",
                            title: "title",
                            message: "      Siber \n\r      版本:" + currentVersion
                        }
                        dialog.showMessageBox(options)


                    }
                },
                {
                    type: 'separator'
                },
                {
                    role: 'services',
                    submenu: []
                },
                {
                    type: 'separator'
                },
                {
                    role: 'hide'
                },
                {
                    role: 'hideothers'
                },
                {
                    role: 'unhide'
                },
                {
                    type: 'separator'
                },
                {
                    role: 'quit'
                }
            ]
        })
        // Edit menu.
        template[1].submenu.push(
            {
                type: 'separator'
            },
            {
                label: 'Speech',
                submenu: [
                    {
                        role: 'startspeaking'
                    },
                    {
                        role: 'stopspeaking'
                    }
                ]
            }
        )
        // Window menu.
        template[3].submenu = [
            {
                label: 'Close',
                accelerator: 'CmdOrCtrl+W',
                role: 'close'
            },
            {
                label: 'Minimize',
                accelerator: 'CmdOrCtrl+M',
                role: 'minimize'
            },
            {
                label: 'Zoom',
                role: 'zoom'
            },
            {
                type: 'separator'
            },
            {
                label: 'Bring All to Front',
                role: 'front'
            }
        ]
    }

    let menu = Menu.buildFromTemplate(template);
    Menu.setApplicationMenu(menu);
}

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
