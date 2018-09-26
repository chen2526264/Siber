import React, {Component} from 'react';
import {HashRouter as Router, Route, Link, Switch} from 'react-router-dom';
import {Layout, Menu, Icon, message} from 'antd';
import Setting from './components/Setting';
import DataSource from './components/DataSource';
import Comparison from './components/Comparison';
import NotFound from './components/NotFound';
import './App.css';

const {Header, Content, Sider} = Layout;
const SubMenu = Menu.SubMenu;

class App extends Component {
  state = {
    selectKeys: ['1']
  }

  componentDidMount() {
    const hash = window.location.hash;
    let key = ''
    switch (hash.substring(1)) {
      case '/':
        key = '1';
        break;
      case '/comparison':
        key = '2';
        break;
      default:
        key = 'not found'
    }
    this.setState({
      selectKeys: [key]
    });
  }

  handleMenuSelect = ({key}) => {
    this.setState({
      selectKeys: [key]
    });
  }

  render() {
    return (<Router>
      <Layout>
        <Header style={{position: 'fixed', width: '100%', zIndex: 100}}>
          <span className="logo-text">siber</span>
          <Menu theme="dark" mode="horizontal" className="menu" selectedKeys={this.state.selectKeys} onSelect={this.handleMenuSelect}>
            <Menu.Item key="1" className="menu-item"><Link to="/">数据源</Link></Menu.Item>
            <Menu.Item key="2" className="menu-item"><Link to="/comparison">分析对比</Link></Menu.Item>
          </Menu>
          <Setting />
        </Header>
        <Content style={{
            background: '#fff',
            margin: '64px 0 20px',
          }}>
          <Switch>
            <Route exact path="/" component={DataSource} />
            <Route exact path="/comparison" component={Comparison} />
            <Route component={NotFound} />
          </Switch>
        </Content>
      </Layout>
    </Router>);
  }
}

export default App;
