import React from 'react';
import {Form, Icon, Input, Button, Select, Row, Col, message} from 'antd';

const FormItem = Form.Item;
const Option = Select.Option;

class DataSourceForm extends React.Component {
  constructor(props) {
    super(props)
    this.state = {

    }
  }

  componentDidMount() {
    console.log('props', this.props);
  }

  handleSubmit = (e) => {
    e.preventDefault();
    this.props.form.validateFieldsAddScroll((err, values) => {
      if (!err) {
        console.log('Received values of form: ', values);
      }
    });
  }

  handleConnectTest = () => {
    const data = this.props.form.getFieldsValue();
    const temp = {
      dataSourceName: data.name,
      dataSourceType: data.type,
      password: data.password,
      schema: data.schema,
      url: data.url,
      userName: data.username
    };
    console.log(temp);
    const url = 'http://localhost:8088/connection';
    const hide = message.loading("正在加载中");
    fetch(url, {
      method: 'POST',
      mode: 'cors',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(temp)
    }).then((response) => {
      if (response.ok) {
        return response.json();
      }
    }).then((data) => {
      hide();
      if (data.code === 0) {
        message.success("连接测试成功");
      } else {
        message.error(data.message);
      }
    }).catch((err) => {
      hide();
      message.error('出错了！');
    });
  }

  render() {
    const {dataSourceTypes} = this.props;
    const {getFieldDecorator} = this.props.form;
    const formItemLayout = {
      labelCol: {
        span: 6
      },
      wrapperCol: {
        span: 16
      },
      style: {
        margin: '8px 0'
      }
    };
    return (
      <Form
        onSubmit={this.handleSubmit}
        style={{marginBottom: 0}}
        hideRequiredMark={true}
      >
        <FormItem {...formItemLayout} label="数据源名称">
          {getFieldDecorator('name', {
            rules: [{
              required: true, message: '请输入数据源名称'
            }]
          })(
            <Input />
          )
        }
        </FormItem>
        <FormItem {...formItemLayout} label="数据库类型">
          {getFieldDecorator('type', {
            rules: [{
              required: true, message: '数据库类型'
            }]
          })(
            <Select>
              {dataSourceTypes.map(type => (
                <Option value={type} key={type}>{type}</Option>
              ))}
            </Select>
          )
        }
        </FormItem>
        <FormItem {...formItemLayout} label="用户名">
          {getFieldDecorator('username', {
            rules: [{
              required: true, message: '请输入用户名'
            }]
          })(
            <Input />
          )
        }
        </FormItem>
        <FormItem {...formItemLayout} label="密码">
          {getFieldDecorator('password', {
            rules: [{
              required: true, message: '请输入密码'
            }]
          })(
            <Input />
          )
        }
        </FormItem>
        <FormItem {...formItemLayout} label="URL">
          {getFieldDecorator('url', {
            rules: [{
              required: true, message: '请输入URL'
            }]
          })(
            <Input />
          )
        }
        </FormItem>
        <FormItem {...formItemLayout} label="Schema">
          {getFieldDecorator('schema', {
            rules: [{
              required: true, message: '请输入schema'
            }]
          })(
            <Input />
          )
        }
        </FormItem>
          <Row>
            <Col span={8} offset={6}>
              <Button onClick={this.handleConnectTest}>连接测试</Button>
            </Col>
          </Row>
      </Form>
    );
  }
}

const onValuesChange = (props, values) => {
  props.handleFormChange(values);
};

const WrappedDataSourceForm = Form.create({
  onValuesChange
})(DataSourceForm);
export default WrappedDataSourceForm;
