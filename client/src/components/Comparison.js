import React from 'react';
import {Collapse, Tag, message} from 'antd';
import StatusList from './StatusList';
import ComparisonTable from './ComparisonTable';
import {Scrollbars} from 'react-custom-scrollbars';

const Panel = Collapse.Panel;
const dataSourceUrl = "http://localhost:8088/datasource/all";
const singleDataSourceUrl = "http://localhost:8088/datasource";

export default class Comparison extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      dataSources: [],
      currentSelectedDataSource: -1,
      needAnalyseTables: [],
      currentTableName: '',
      compareDatas: [],
      viewHeight: 0
    }
  }

  // 组件加载完成之后执行
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
    this.fetchDataSource();
  }

  // 设置和记录当前选择的是哪一个数据源
  handleDAtaSourceSelected = (value) => {
    this.setState({
      currentSelectedDataSource: value
    });
  }

  // 获取数据源并设置state的方法
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
        message.error(data.message);
      }
    }).catch((e) => {
      hide();
      message.error("出错了")
    })
  }

  // 选择某个
  fetchSelectedTables = (dataSourceId) => {
    const hide = message.loading("正在加载中");
    fetch(singleDataSourceUrl+'?id='+dataSourceId).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        const needAnalyseTablesTemp = [];
        data.result.tables && data.result.tables.map(item => {
          if (!item.needAnalyse) {
            needAnalyseTablesTemp.push(item.tableName);
          }
        });
        console.log(needAnalyseTablesTemp.length);
        this.setState({
          needAnalyseTables: needAnalyseTablesTemp
        })
      } else {
        hide();
        message.error(data.message);
      }
    })
  }

  // 用来获取当前数据源的名字
  passDataSourceName = () => {
    let name = ''
    if (this.state.currentSelectedDataSource) {
      this.state.dataSources.map((item) => {
        if (item.id === this.state.currentSelectedDataSource) {
          name = item.dataSourceName;
        }
      });
    }
    return name;
  }

  // 清除对比的数据
  cleanCompareDatas = () => {
    this.setState({
      compareDatas: []
    });
  }

  setCompareDatas = (values) => {
    this.setState({
      compareDatas: values
    });
  }

  getComparedTables = () => {
    let tables = [];
    this.state.compareDatas.map(item => {
      tables.push(item.table.tableName);
    })
    return tables;
  }

  getTableColumnsByName = () => {
    let columns = [];
    this.state.compareDatas.map(item => {
      if (item.table.tableName == this.state.currentTableName) {
        columns = item.table.columns;
      }
    });
    return columns;
  }

  getComparedDataByName = () => {
    let data = [];
    this.state.compareDatas.map((item) => {
      if (item.table.tableName == this.state.currentTableName) {
        item.compareRows.map((item, index) => {
          let rowObj = {}
          rowObj.key = index;
          item.columns.map((item, index) => {
            rowObj[index] = item
          });
          data.push(rowObj);
        });
      }
    });
    console.log(data);
    return data;
  }

  getSqlsByName = () => {
    let data = [];
    this.state.compareDatas.map((item) => {
      if (item.table.tableName == this.state.currentTableName) {
        data = item.sqls;
      };
    });
    return data;
  }

  render() {
    const contentStyle = {
      background: '#fff',
      marginLeft: 216
    };

    const customPanelStyle = {
      background: '#f7f7f7',
      borderRadius: 4,
      marginBottom: 8,
      border: 0,
      overflow: 'hidden'
    }
    return (
      <div style={{background: '#f0f2f5'}}>
        <StatusList
          dataSources={this.state.dataSources}
          handleDAtaSourceSelected={this.handleDAtaSourceSelected}
          currentSelectedDataSource={this.state.currentSelectedDataSource}
          cleanCompareDatas={this.cleanCompareDatas}
          setCompareDatas={this.setCompareDatas}
          setSaveAndCompareStatus={this.setSaveAndCompareStatus}
        />
        <div style={contentStyle}>
          <Scrollbars style={{height: this.state.viewHeight - 28}}>
          <Collapse bordered={false} defaultActiveKey={['1']}>
            <Panel header="打开/关闭 Table列表" style={customPanelStyle} key="1">
              {this.getComparedTables().map(item => (
                <Tag style={{margin: '2px'}}
                  key={item}
                  data-name={item}
                  onClick={(e) => {
                    console.log('@@@@@@');
                    this.setState({
                      currentTableName: e.target.getAttribute('data-name')
                    });
                    console.log('@@@@@@#####');
                  }}
                >
                  {item}
                </Tag>
              ))}
            </Panel>
          </Collapse>
          <ComparisonTable
            currentSelectedDataSource={this.passDataSourceName()}
            currentTableName={this.state.currentTableName}
            getTableColumnsByName={this.getTableColumnsByName}
            getComparedDataByName={this.getComparedDataByName}
            getSqlsByName={this.getSqlsByName}
          />
        </Scrollbars>
        </div>
      </div>
    );
  }
}
