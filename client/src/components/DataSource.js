import React from 'react';
import {message} from 'antd';
import DataSourceList from './DataSourceList';
import TableFields from './TableFields';
import {Scrollbars} from 'react-custom-scrollbars';

const dataSourceUrl = "http://localhost:8088/datasource/all";
const dataSourceTypesUrl = 'http://localhost:8088/datasource/types';
const testUrl = "http://localhost:8088/test";

export default class DataSource extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      whitchTableSelected: null,
      whitchDataSourceIdSelected: null,
      dataSources: [],
      tableData: [],
      isToCompare: false,
      viewHeight: 0,
      dataSourceTypes: [],
    }
  }


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
    this.testServer();
    this.timer = setInterval(this.testServer, 3000);
  }

  componentWillUnmount() {
    clearInterval(this.timer);
  }

  testServer = () => {
    fetch(testUrl).then((response) => {
      if (response.ok) {
        return response.text();
      }
    }).then(data => {
      if (data === "success") {
        this.fetchDataSource();
        this.fetchTypes();
        clearInterval(this.timer)
      } else {
        message.info("后台服务正在启动中");
      }
    }).catch((err) => {
      message.info("后台服务正在启动中");
    });
  }


  fetchDataSource = () => {
    const hide = message.loading("正在加载中");
    fetch(dataSourceUrl, {
      method: 'GET',
      mode: 'cors',
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        this.setState({
          dataSources: data.result
        });
      } else {
        message.error("出错了")
      }
    }).catch((e) => {
      hide();
      message.error("网络出问题了")
    })
  }

  fetchTypes = () => {
    const hide = message.loading("正在加载中");
    fetch(dataSourceTypesUrl, {
      method: 'GET',
      mode: "cors",
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        this.setState({
          dataSourceTypes: data.result
        })
      } else {
        message.error(data.message);
      }
    }).catch((e) => {
      hide();
      message.error("出错了！");
    })
  }


  fetchDataSourceTable = (datasourceId, tableName) => {
    const url = 'http://localhost:8088/table?datasourceId=' + datasourceId + '&tableName=' + tableName;
    const hide = message.loading("正在加载中");
    fetch(url).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        this.setState({
          tableData: data.result.columns,
          whitchTableSelected: tableName,
          whitchDataSourceIdSelected: datasourceId,
          isToCompare: data.result.needAnalyse
        })
      } else {
        message.error(data.message)
      }
    }).catch((err) => {
      hide();
      message.error('网络出错了！')
    })
  }


  handleTableSave = () => {
    const {whitchTableSelected, whitchDataSourceIdSelected, tableData, isToCompare} = this.state;
    const url = 'http://localhost:8088/table/update';
    const hide = message.loading("正在加载中");
    const data = {
      datasourceId: parseInt(whitchDataSourceIdSelected),
      needAnalyse: isToCompare,
      tableName: whitchTableSelected,
      columns: tableData
    };
    fetch(url, {
      method: 'POST',
      mode: 'cors',
      body: JSON.stringify(data),
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
        message.success('成功更新表');
      } else {
        message.error(data.message)
      }
    }).catch((err) => {
      hide();
      message.error("出错了！");
    });
  }


  handleKeyChange = (name, value) => {
    let tempData = this.state.tableData;
    tempData.map(item => {
      if (item.columnName == name) {
        item.pk = value
      }
    });
    this.setState({
      tableData: tempData
    })
  }


  handleTableRefresh = () => {
    const id = this.state.whitchDataSourceIdSelected;
    const url = 'http://localhost:8088/table/refresh?id=' + id;
    const hide = message.loading("正在加载中");
    fetch(url, {
      method: 'POST',
      mode: 'cors'
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        message.success('成功刷新了表格！');
        this.fetchDataSource();
      }
    }).catch((err) => {
      message.error("出错了！")
    });
  }


  render() {
    const contentStyle = {
      background: '#fff',
      marginLeft: 216,
    };
    return (
      <div style={{background: '#f0f2f5'}}>
        <DataSourceList
          sourceData={this.state.dataSources}
          dataSourceTypes={this.state.dataSourceTypes}
          fetchDataSource={this.fetchDataSource}
          fetchDataSourceTable={this.fetchDataSourceTable}
          handleTableRefresh={this.handleTableRefresh}
        />
      <div style={contentStyle}>
        <Scrollbars style={{
            height: this.state.viewHeight - 28
          }}>
          <TableFields
            data={this.state.tableData}
            isToCompare={this.state.isToCompare}
            isToCompareChange={() => {
              this.setState({
                isToCompare: !this.state.isToCompare
              });
            }}
            handleTableSave={this.handleTableSave}
            isSaveCanUse = {this.state.whitchTableSelected === null ? true : false}
            handleKeyChange={this.handleKeyChange}
          />
      </Scrollbars>
    </div>
      </div>

    );
  }
}
