import React from 'react';
import {Menu, Icon, Dropdown, Popconfirm, Button, message, Modal} from 'antd';
import AddDataSource from './AddDataSource';
import DataSourceForm from './DataSourceForm';
import {Scrollbars} from 'react-custom-scrollbars';

const SubMenu = Menu.SubMenu;
const contextSpaces = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';
const singleDataSourceUrl = 'http://localhost:8088/datasource';


export default class DataSourceList extends React.Component {
  rootSubmenuKeys = [];
  state = {
    openKeys: [],
    whichDataSourceId: '',
    editModalVisible: false,
    formDatas: {
      name: '',
      type: '',
      username: '',
      password: '',
      url: '',
      schema: '',
    },
    viewHeight: 0,
  };

  onContextMenuClick = ({keyPath, item, key, domEvent}) => {
    domEvent.stopPropagation();
    // 单击编辑选项，显示modal，并获取当前数据源的数据
    if (key=="c1") {
      const hide = message.loading("正在加载中");
      fetch(singleDataSourceUrl+'?id='+this.state.whichDataSourceId).then((response) => {
        if (response.ok) {
          return response.json();
        }
      }).then((data) => {
        hide();
        if (data.code===0) {
          this.setState({
            editModalVisible: true,
            formDatas: {
              name: data.result.dataSourceName,
              type: data.result.dataSourceType,
              username: data.result.userName,
              password: data.result.password,
              url: data.result.url,
              schema: data.result.schema,
            }
          });
          this.form.setFieldsValue(this.state.formDatas);
        }
      }).catch((err) => {
        hide();
        message.error("出错了！")
      })
    }
  };

  contextMenu = (
    <Menu onClick={this.onContextMenuClick}>
      <Menu.Item key="c1"><Icon type="edit"/>&nbsp;&nbsp; 编 &nbsp;&nbsp;辑 &nbsp;&nbsp;</Menu.Item>
      <Menu.Item key="c2"><Icon type="delete"/>
        <Popconfirm
          title="确定删除?"
          okText="删除"
          cancelText="取消"
          placement="rightTop"
          onConfirm={(e) => {
            this.handleDeleteDataSource();
          }}
        >
          &nbsp;&nbsp; 删 &nbsp;&nbsp;除 &nbsp;&nbsp;
        </Popconfirm>
      </Menu.Item>
    </Menu>
  );

  componentDidMount() {
    var myViewHieght = window.innerHeight;
    this.setState({
      viewHeight: myViewHieght - 64
    });
    window.addEventListener('resize',  () => {
      var myViewHieght = window.innerHeight;
      this.setState({
        viewHeight: myViewHieght - 64
      });
    });
  }

  /**
   * 编辑数据源保存回调
   */
  handleEditModalOk = (e) => {
    const formDatas = this.state.formDatas;

    const data = {
        id: this.state.whichDataSourceId,
        dataSourceName: formDatas.name,
        dataSourceType: formDatas.type,
        password: formDatas.password,
        schema: formDatas.schema,
        url: formDatas.url,
        userName: formDatas.username
    }
    const hide = message.loading("正在加载中");
    fetch(singleDataSourceUrl, {
      mode: 'cors',
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      this.setState({
        editModalVisible: false
      });
      if (data.code === 0) {
        message.success("成功更新了数据源")
        this.props.fetchDataSource();
      }
    }).catch((e) => {
      hide();
      this.setState({
        editModalVisible: false
      });
      message.error("网络错误")
    });
    this.form.resetFields();
  }

  /**
   * 编辑数据源对话框取消，此时清空表格
   */
  handleEditModalCancel = (e) => {
    console.log('modal cancel clicked');
    this.setState({
      editModalVisible: false
    });
  }

  handleRightMouseClick = (e) => {
    if (e.button === 2) { // 鼠标右键点击
      let id = e.target.getAttribute('data-sourceid');
      this.setState({
        whichDataSourceId: id
      })
    }
  }

  handleDeleteDataSource = (e) => {
    const hide = message.loading("正在加载中");
    fetch(singleDataSourceUrl+'?id='+this.state.whichDataSourceId, {
      method: 'DELETE',
      mode: 'cors',
      headers: {
        'Content-Type': 'application/json'
      }
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        message.success("成功删除数据源");
        this.props.fetchDataSource();
      } else {
        message.error(data.message);
      }
    }).catch((err) => {
      hide();
      message.error("网络出问题了！");
    })
  }

  onOpenChange = (openKeys) => {
    const latestOpenKey = openKeys.find(key => this.state.openKeys.indexOf(key) === -1);
    if (this.rootSubmenuKeys.indexOf(latestOpenKey) === -1) {
      this.setState({ openKeys });
    } else {
      this.setState({
        openKeys: latestOpenKey ? [latestOpenKey] : [],
      });
    }
  }

  dataSourceFormRef = (form) => {
    this.form = form;
  }

  handleFormChange = (formdata) => {
    this.setState((prevState) => {
      formDatas: Object.assign(prevState.formDatas, formdata)
    });
  }

  handleDataSourceTableClick = ({key, domEvent}) => {
    if (domEvent) {
      const data = key.split('/');
      this.props.fetchDataSourceTable(data[0], data[1]);
    }
  }

  handleRefreshTable = (id) => {
    const url = 'http://localhost:8088/table/refresh?id=' + id;
    const hide = message.loading("正在加载中");
    fetch(url, {
      method: 'POST',
      mode: 'cors',
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        message.success('数据表更新成功');
        this.props.fetchDataSource();
      } else {
        message.error(data.message)
      }
    }).catch((err) => {
      hide();
      message.error('出错了！')
    });
  }

  render() {
    const siderStyle = {
      width: 200,
      position: 'fixed',
      left: 0,
      top: 64,
      height: this.state.viewHeight,
      background: '#fff',
    };
    this.props.sourceData.map(item => {
      this.rootSubmenuKeys.push(item.id+'');
    });
    return (
      <Scrollbars style={siderStyle} autoHide>
        <AddDataSource dataSourceTypes={this.props.dataSourceTypes} fetchDataSource={this.props.fetchDataSource}/>
        <Menu
          mode="inline"
          openKeys={this.state.openKeys}
          onOpenChange={this.onOpenChange}
          style={{ width: 200, border: 0 }}
          onClick={this.handleDataSourceTableClick}
        >
          {this.props.sourceData.map((item) => {
            return (
              <SubMenu key={item.id}
                title={<span><Icon type="database" />
                  <Dropdown overlay={this.contextMenu} trigger={['contextMenu']} >
                    <span onMouseDown={this.handleRightMouseClick} data-sourceid={item.id}>{item.dataSourceName}
                      <span dangerouslySetInnerHTML={{__html: contextSpaces}} data-sourceid={item.id}></span>
                    </span>
                  </Dropdown>
                </span>}
              >
                <Popconfirm
                  title="确定刷新数据源table?"
                  okText="刷新"
                  cancelText="取消"
                  placement="rightTop"
                  onConfirm={() => {
                    this.handleRefreshTable(item.id);
                  }}
                >
                  <div
                    style={{margin: '5px 24px', background: '#f0f2f5', textAlign: 'center', cursor: 'pointer'}}
                  >
                    <span style={{lineHeight: '28px'}}><Icon type="reload"/>刷新表信息</span>
                  </div>
                </Popconfirm>
                {item.tables && item.tables.map(tableItem => (<Menu.Item key={item.id+"/"+tableItem.tableName}>{tableItem.tableName}</Menu.Item>))}
              </SubMenu>
            );
          })}
        </Menu>
        <Modal
          title="编辑数据源"
          visible={this.state.editModalVisible}
          onOk={this.handleEditModalOk}
          onCancel={this.handleEditModalCancel}
          okText="保存"
          cancelText="关闭"
        >
        <DataSourceForm
          ref={this.dataSourceFormRef}
          dataSourceTypes={this.props.dataSourceTypes}
          handleFormChange={this.handleFormChange}
        />
      </Modal>
      </Scrollbars>
    );
  }
}
