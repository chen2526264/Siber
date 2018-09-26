import React from 'react';
import {Modal, Button, Form, Input, Icon} from 'antd';

/**
 * 全局设置的组件
 * @type {Object}
 */
export default class Setting extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      visible: false,
      confirmLoading: false,
    }
  }

  showModal = () => {
    this.setState({
      visible: true
    });
  }

  handleOk = () => {
    this.setState({
      confirmLoading: true
    });

    setTimeout(() => {
      this.setState({
        visible: false,
        confirmLoading: false
      });
    }, 2000);
  }

  handleCancel = () => {
    console.log("clicked cancel button");
    this.setState({
      visible: false
    });
  }

  render() {
    const {visible, confirmLoading} = this.state;
    return (
      <div>
        <Icon style={{position: 'fixed', top: 16, right: 48, fontSize: '30px', cursor: 'pointer', color: 'white'}} onClick={this.showModal} type="setting" />
        <Modal title="设置"
          visible={visible}
          onOk={this.handleOk}
          confirmLoading={confirmLoading}
          onCancel={this.handleCancel}
          okText="确认"
          cancelText="取消"
        >
          <Form.Item label="最大状态记录">
            <Input placeholder="10"/>
          </Form.Item>
        </Modal>
      </div>
    );
  }

}
