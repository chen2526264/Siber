import React from 'react';
import {Table, Icon, Checkbox, Switch, Button} from 'antd';
import {Scrollbars} from 'react-custom-scrollbars';

const tableStyle = {
  width: 650,
  marginLeft: 16,
};

export default class TableFields extends React.Component {
  constructor(props) {
    super(props)
  }

  componentDidMount() {

  }

  columns = [{
      title: "字段名",
      dataIndex: 'columnName',
      // key: 'columnName',
      width: '65%',
  },{
    title: '设置主键',
    render: (text, record) => {
      return (
      <Checkbox onChange={(e) => {
        const value = e.target.checked;
        const name = text.columnName;
        this.props.handleKeyChange(name, value);
      }} checked={text.pk}><Icon type="key"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</Checkbox>
    )}
  }];

  render() {
    return (
      <div>
        <Table
          style={tableStyle}
          columns={this.columns}
          dataSource={this.props.data}
          pagination={false}
          bordered={true}
        />
        <div style={{margin: '12px 16px'}}>
          <Switch
            checkedChildren={<Icon type="check" />}
            unCheckedChildren={<Icon type="cross" />}
            checked={this.props.isToCompare}
            onChange={this.props.isToCompareChange}
            disabled={this.props.isSaveCanUse}
          />
        <span>&nbsp;&nbsp;是否加入对比分析</span>
        </div>
        <Button
          type="primary"
          style={{margin: '0 16px', width: 120, marginBottom: 24}}
          disabled={this.props.isSaveCanUse}
          onClick={this.props.handleTableSave}
        >
          保&nbsp;&nbsp;存
        </Button>
      </div>
    );
  }
}
