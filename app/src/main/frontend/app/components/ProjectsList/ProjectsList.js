import React, {Component} from 'react';
import {FormattedTime} from 'react-intl';
import {connect} from 'react-redux'
import PropTypes from "prop-types";
import {
  listProjects,
  refreshProject,
  selectProject
} from './ProjectsListActions';
import {bindActionCreators} from 'redux'
import {
  Badge,
  Card,
  Col,
  Divider,
  Icon,
  Input,
  message,
  Row,
  Switch,
  Table
} from 'antd';
import TechnologyIndicator from "../TechnologyIndicator/TechnologyIndicator";
import './ProjectsList.css';

const Column = Table.Column;
const Search = Input.Search;

export class ProjectsList extends Component {
  componentWillMount() {
    this.props.listProjects(this.props.pageNumber, this.props.pageSize,
        this.props.search);
  }

  componentWillUpdate(nextProps) {
    if (this.props.refreshLoading && !nextProps.refreshLoading) {
      if (nextProps.refreshError) {
        message.error(nextProps.refreshErrorMessage, 5);
      } else {
        message.success("Refresh finished");
      }
    }
  }

  handlePagination(page) {
    this.props.listProjects(page - 1, this.props.pageSize, this.props.search);
  }

  searchByProjectName(value) {
    let search = {};
    search.name = value ? value : null;
    Object.assign(this.props.search, search);
    this.props.listProjects(0, this.props.pageSize, this.props.search);
  }

  onChangeDependencySwitch(value) {
    this.props.search.onlyWithDependencyManager = value;
    this.props.listProjects(0, this.props.pageSize, this.props.search);
  }

  refreshProject(projectId) {
    this.props.refreshProject(projectId);
  }

  selectProject(project, e) {
    this.props.selectProject(project);
    e.preventDefault();
    console.log(e);
  }

  render() {
    const pagination = {
      total: this.props.totalItems,
      showTotal: total => 'Total of ' + total + ' projects',
      pageSize: this.props.pageSize,
      current: this.props.pageNumber + 1,
      onChange: this.handlePagination.bind(this)
    };

    return (
        <Card title={this.props.title}>
          <Row className="filters">
            <Col span={12} className="col1">
              <Search
                  placeholder="search by project name"
                  style={{width: 250}}
                  onSearch={this.searchByProjectName.bind(this)}
                  enterButton
              />
            </Col>
            <Col span={12} className="col2">
              <Switch onChange={this.onChangeDependencySwitch.bind(this)}
                      checked={this.props.search.onlyWithDependencyManager}/> Only
              with dependency manager
            </Col>
          </Row>
          <Divider/>
          <Table dataSource={this.props.projects}
                 rowKey={record => record.projectId}
                 loading={this.props.loading} className={'projects'}
                 pagination={pagination} size="small">
            <Column
                title="Name"
                dataIndex="name"
                key="name"
                width="30%"
                render={(text, record) => {
                  let result = [];
                  if (record.lastAnalyzeExecutionHasFallback) {
                    result.push(<Icon key="ief" type="close-circle"
                                      theme="filled"
                                      className="icon-execution-fallback"
                                      title="One or more fallbacks was executed in the last analyze"/>);
                    result.push(" ");
                  }
                  result.push(text);
                  result.push(" ");
                  result.push(<Badge count={record.unstableArtifactsCount}
                                     key={"b-" + record.id}
                                     showZero={false}/>);

                  return (<span>{result}</span>);
                }}
            />
            <Column
                title="Branch"
                dataIndex="branch"
                key="branch"
                width="10%"
            />
            <Column
                title="Last updated"
                dataIndex="lastCollectDate"
                key="lastCollectDate"
                width="15%"
                render={(text) => (
                    <FormattedTime
                        value={text}
                        day="numeric"
                        month="numeric"
                        year="numeric"/>
                )}/>
            <Column
                title="Technologies"
                dataIndex="relatedTechnologies"
                key="technologies"
                width="35%"
                render={(techs) => {
                  let result = [];
                  techs.forEach(tech => {
                    result.push(<TechnologyIndicator name={tech} key={tech}/>);
                  });
                  return (result);
                }}
            />
            <Column
                title="Actions"
                key="action"
                width="10%"
                render={(text, record) => (
                    <span>
                          <a href={record.scmRepository.url} target={"_blank"}>
                            <Icon type="github" className={'action-btn'}/>
                          </a>
                      {this.props.isAdmin ? <a href={"#"}
                                               onClick={this.refreshProject.bind(
                                                   this,
                                                   record.projectId)}
                                               title={"Refresh"}>
                        <Icon type="reload" className={'action-btn'}/>
                      </a> : null
                      }
                      <a href={"#"}
                         onClick={this.selectProject.bind(this, record)}
                         title={"View descriptors"}>
                            <Icon type="plus-circle-o"
                                  className={'action-btn'}/>
                          </a>
                        </span>
                )}/>
          </Table>
        </Card>
    )
  }
}

ProjectsList.propTypes = {
  title: PropTypes.string
};

const mapStateToProps = (state) => {
  return {
    projects: state.projects.list,
    pageSize: state.projects.pageSize,
    pageNumber: state.projects.pageNumber,
    totalItems: state.projects.totalItems,
    loading: state.projects.loading,
    refreshLoading: state.projects.refreshLoading,
    refreshError: state.projects.refreshError,
    refreshErrorMessage: state.projects.refreshErrorMessage,
    search: state.projects.search,
    isAdmin: state.auth.logged ? state.auth.auth.isAdmin : false
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators({listProjects, selectProject, refreshProject},
      dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(ProjectsList)