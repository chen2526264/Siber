import React from 'react';
import {List, Checkbox, message, Tooltip} from 'antd';
import FilterDataSource from './FilterDataSource';
import {Scrollbars} from 'react-custom-scrollbars';
import './StatusList.css'

const statusListUrl = 'http://localhost:8088/compare/status';

export default class StatusList extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      statusList: [],
      selectedStatus: [],
      viewHeight: 0
    };
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
  }

  // 根据数据源id获取状态
  fetchStatusById = (dataSourceId) => {
    fetch(statusListUrl+'?id='+dataSourceId).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      if (data.code === 0) {
        message.info("已成功获取状态")
        this.setState({
          statusList: data.result,
        })
      } else {
        message.error(data.message);
      }
    }).catch((err) => {
      message.error("出错了！")
    });
  }

  // 尤其注意这个方法中prevState的用法
  handleStatusSelect = (e) => {
    const name = e.target['data-id'];
    if (e.target.checked) {
      this.setState((prevState) => ({
        selectedStatus: [...prevState.selectedStatus, name]
      }));
    } else {
      const index = this.state.selectedStatus.indexOf(name);
      if (index > -1) {
        const temp = this.state.selectedStatus;
        temp.splice(index, 1);
        this.setState({
          selectedStatus: temp,
        });
      }
    }
    // 下面的方法是错的，因为state的变化是在下一个周期，这个时候取到的值是上一轮的
    // if (this.state.selectedStatus.length > 1) {
    //   console.log('length > 1');
    // }
  }

  handleCompare = () => {
    if (this.state.selectedStatus.length == 2) {
      const a = this.state.selectedStatus[0];
      const b = this.state.selectedStatus[1];
      var oldId, newId;
      if (a > b) {
        oldId = b;
        newId = a;
      } else {
        oldId = a;
        newId = b;
      }
      const url = 'http://localhost:8088/compare?datasourceId='+this.props.currentSelectedDataSource
        + '&oldStatusId='+oldId + '&newStatusId='+newId;
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
          message.success('对比完成！')
          this.props.setCompareDatas(data.result);
        } else {
          message.error(data.message)
        }
      }).catch((err) => {
        hide();
        message.error('出错了！');
      });
    } else {
      message.error("只有1个数据源的2个状态才能对比")
    }
  }

  cleanSelectedStatus = () => {
    this.setState({
      selectedStatus: []
    })
  }

  handleStatusSave = () => {
    if (this.props.currentSelectedDataSource === -1) {
      message.error("请先选择一个数据源")
    } else {
      const url = 'http://localhost:8088/compare/save?id=' + this.props.currentSelectedDataSource;
      const hide = message.loading("正在加载中");
      fetch(url, {
        method: 'POST',
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
          message.success("状态保存成功");
          this.fetchStatusById(this.props.currentSelectedDataSource);
        }
      }).catch((err) => {
        hide();
        message.error("出错了！")
      });
    }
  }

  // setSaveAndCompareStatus = () => {
  //   const temp = [];
  //   this.state.statusList.slice(-2).map((item) => {
  //     temp.push(item.id)
  //   });
  //   console.log(temp);
  //   this.setState({
  //     selectedStatus: temp,
  //   })
  // }

  handleSaveAndCompare = () => {
    if (this.props.currentSelectedDataSource === -1) {
      message.error("请先选择一个数据源")
    } else {
      const url = 'http://localhost:8088/compare/saveAndCompare?datasourceId='+this.props.currentSelectedDataSource;
      fetch(url, {
        method: 'POST',
        mode: 'cors'
      }).then((response) => {
        if (response.ok) {
          return response.json();
        }
      }).then((data) => {
        if (data.code === 0) {
          message.success('保存并对比完成！');
          this.fetchStatusById(this.props.currentSelectedDataSource);
          this.props.setCompareDatas(data.result);
          // this.setSaveAndCompareStatus();
        } else {
          message.error(data.message)
        }
      }).catch((err) => {
        message.error('出错了！');
      });
    }
  }

  handleStrLength = (str) => {
    if (str.length > 19) {
      return str.slice(0, 19) + '...';
    }
    return str;
  }

  render() {
    const siderStyle = {
      width: 200,
      position: 'fixed',
      left: 0,
      top: 64,
      background: '#fff',
      height: this.state.viewHeight
    };
    return (
      <Scrollbars style={siderStyle}>
        <FilterDataSource
          dataSources={this.props.dataSources}
          fetchStatusById={this.fetchStatusById}
          handleDAtaSourceSelected={this.props.handleDAtaSourceSelected}
          selectedStatus={this.state.selectedStatus}
          currentSelectedDataSource={this.props.currentSelectedDataSource}
          handleCompare={this.handleCompare}
          cleanSelectedStatus={this.cleanSelectedStatus}
          cleanCompareDatas={this.props.cleanCompareDatas}
          setCompareDatas={this.props.setCompareDatas}
          handleStatusSave={this.handleStatusSave}
          handleSaveAndCompare={this.handleSaveAndCompare}
        />
        <List
          dataSource={this.state.statusList}
          renderItem={item => (<List.Item style={{
            paddingLeft: 8,
            whiteSpace: 'normal',
          }}
          key={item.id}
        >
          <Checkbox
            style={{marginRight: 20}}
            onChange={this.handleStatusSelect}
            data-id={item.id}
          >
            <Tooltip title={item.id + '_' + item.name}>
              <span>{this.handleStrLength(item.id + '_' + item.name)}</span>
            </Tooltip>
          </Checkbox>
        </List.Item>)}
        />
    </Scrollbars>
  );
  }
}
