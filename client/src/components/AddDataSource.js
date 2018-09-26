import React from 'react';
import {Modal, Button, Form, Input, Icon, message} from 'antd';
import DataSourceForm from './DataSourceForm';

const addDataSourceZone = {
  cursor: 'pointer',
  width: 200,
  lineHeight: '48px',
  background: '#001529',
  color: 'white',
  textAlign: 'center',
  borderTop: '1px solid #f0f2f5',
};

const createDataSourceUrl = "http://localhost:8088/datasource";

export default class AddDataSource extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      confirmLoading: false,
      formDatas: {
        name: '',
        type: '',
        username: '',
        password: '',
        url: '',
        schema: '',
      },
    }
  }

  showModal = () => {
    this.setState({
      visible: true
    });
  }

  handleOk = () => {
    const formDatas = this.state.formDatas;
    this.setState({
      confirmLoading: true
    });

    const data = {
        dataSourceName: formDatas.name,
        dataSourceType: formDatas.type,
        password: formDatas.password,
        schema: formDatas.schema,
        url: formDatas.url,
        userName: formDatas.username
    }

    fetch(createDataSourceUrl, {
      mode: 'cors',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    }).then((response) => {
      console.log(response);
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      console.log(data);
      this.setState({
        confirmLoading: false,
        visible: false
      });
      if (data.code === 0) {
        message.success("成功创建数据源")
        this.props.fetchDataSource();
      }
    }).catch((e) => {
      this.setState({
        confirmLoading: false,
        visible: false
      });
      this.setState({
        confirmLoading: false,
        visible: false
      });
      message.error("网络错误")
    });
    this.form.resetFields();
  }

  handleCancel = () => {
    this.setState({
      visible: false
    });
  }

  handleFormChange = (formdata) => {
    this.setState((prevState) => {
      formDatas: Object.assign(prevState.formDatas, formdata)
    });
  }

  dataSourceFormRef = (form) => {
    this.form = form
  }

  render() {
    const {visible, confirmLoading} = this.state;
    return (
      <div>
        <div style={addDataSourceZone} onClick={this.showModal}>+ 添加数据源</div>
        <Modal title="数据源"
          visible={visible}
          onOk={this.handleOk}
          confirmLoading={confirmLoading}
          onCancel={this.handleCancel}
          okText="保存"
          cancelText="取消"
        >
          <DataSourceForm
            ref={this.dataSourceFormRef}
            dataSourceTypes={this.props.dataSourceTypes}
            handleFormChange={this.handleFormChange}
            clearFormData={this.clearFormData}
          />
        </Modal>
      </div>
    );
  }

}
