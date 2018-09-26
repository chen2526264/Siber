import React from 'react';
import {Divider, Select, Popconfirm, Icon, Tooltip} from 'antd';

const Option = Select.Option;
const actionStyle = {
  textAlign: 'center',
  lineHeight: '48px',
  cursor: 'pointer',
  display: 'inline-block',
  height: '48px',
  width: '60px',
  fontSize: '12px'
};

export default class FilterDataSource extends React.Component {
  constructor(props) {
    super(props)

  }

  render() {
    return (
      <div style={{width: 200, background: '#001529', borderTop: '1px solid #f0f2f5'}}>
        <Select
          showSearch
          style={{width: 180, margin: '10px 10px'}}
          placeholder="请选则一个数据源"
          filterOption={(input, option) => option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0}
          onSelect={(value) => {
            this.props.handleDAtaSourceSelected(value);
            this.props.fetchStatusById(value);
            this.props.cleanSelectedStatus();
            this.props.cleanCompareDatas();
          }}
        >
          {this.props.dataSources.map(item => (
            <Option value={item.id} key={item.id}>{item.dataSourceName}</Option>
          ))}
        </Select>
        <div style={{color: '#fff', padding: 0}}>
          <Popconfirm
            okText="保存"
            cancelText="取消"
            title={<span><Icon type="info-cicle"/>
              {this.props.currentSelectedDataSource === -1  ? '必须选择1个数据源' : '确认保存状态？'}</span>}
            placement="rightTop"
            onConfirm={this.props.handleStatusSave}
          >
            <Tooltip title="保存一个新状态，目前最多10个，新的会覆盖旧的" placement="bottomLeft">
              <span style={actionStyle}>保存状态</span>
            </Tooltip>
          </Popconfirm>
          <Divider type="vertical" style={{margin: 4}}/>
          <Popconfirm
            okText="确认"
            cancelText="取消"
            title={<span><Icon type="info-cicle"/>
              {this.props.currentSelectedDataSource === -1 || this.props.selectedStatus.length !== 2 ? '必须选择1个数据源和2个状态' : '确认开始对比？'}</span>}
            placement="rightTop"
            onConfirm={this.props.handleCompare}
          >
            <Tooltip title="任意选择两个状态进行对比" placement="bottomLeft">
              <span style={actionStyle}>选择对比</span>
            </Tooltip>
          </Popconfirm>
          <Divider type="vertical" style={{margin: 4}}/>
            <Popconfirm
              okText="确认"
              cancelText="取消"
              title={<span><Icon type="info-cicle"/>
                {this.props.currentSelectedDataSource === -1  ? '必须选择1个数据源' : '保存当前状态并与之前的状态对比？'}</span>}
              placement="rightTop"
              onConfirm={this.props.handleSaveAndCompare}
            >
              <Tooltip title="保存当前状态，并和之前最新的状态进行对比" placement="bottomLeft">
                <span style={actionStyle}>立即对比</span>
              </Tooltip>
            </Popconfirm>
        </div>
      </div>
    );
  }
}
