import React from 'react';
import {Breadcrumb, Table, List, Card, Divider, Tabs} from 'antd';
import './ComparisonTable.css';

const TabPane = Tabs.TabPane;

const whitchBackground = (type) => {
  switch (type) {
    case 0:  // 未改变
      return 'no-change';
    case 2:   // 新增
      return 'new-val';
    case 1:   // 改变
      return 'change-val';
    case 3:   // 删除
      return 'del-val';
    default:
      return ''
  }
};

export default class ComparisonTable extends React.Component {
  constructor(props) {
    super(props)

  }

  calcColumns = () => {
    let columns = []
    const columnNames = this.props.getTableColumnsByName();
    const count = columnNames.length;
    let percent = '100%'
    if (count != 0) {
      percent = (1 / count)*100 + '%'
    }
    columnNames.map((item, index) => {
      columns.push({
        title: item.columnName,
        dataIndex: index,
        key: index,
        width: percent,
        className: 'tablecell',
        render: (text, record) => {
          return (
            <div style={{width: '100%', height: '48px'}}
              className={whitchBackground(text.status)}
            >
              {!text.oldValue ? <span>&nbsp;</span> : text.oldValue}
              <Divider style={{margin: '3px 0'}}/>
              {!text.newValue ? <span>&nbsp;</span> : text.newValue}
            </div>
          );
        }
      });
    });
    return columns;
  }

  render() {
    return (
      <div>
        <Breadcrumb style={{marginLeft: 16, marginBottom: 8}}>
          <Breadcrumb.Item>{'数据源: '+this.props.currentSelectedDataSource}</Breadcrumb.Item>
          <Breadcrumb.Item>{'数据表: ' + this.props.currentTableName}</Breadcrumb.Item>
        </Breadcrumb>
        <Tabs defaultActiveKey="1" style={{marginBottom: 0}}>
          <TabPane tab="表格" key="1" >
            <Table columns={this.calcColumns()}
              scroll={{x: 960}}
              dataSource={this.props.getComparedDataByName()}
              pagination={false}
              rowClassName="table-row"
              bordered
            />
          </TabPane>
          <TabPane tab="SQL语句" key="2" style={{padding: 5}}>
            <List
              bordered
              dataSource={this.props.getSqlsByName()}
              renderItem={(item, index) => (<List.Item key={index} style={{padding: '0 24', wordBreak: 'break-all'}}>{item}</List.Item>)}
            />
          </TabPane>
        </Tabs>
      </div>
    )
  }
}
