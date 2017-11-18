import React, {Component} from 'react';
import {FormattedTime} from 'react-intl';
import * as PropTypes from "react/lib/ReactPropTypes";
import {connect} from 'react-redux'
import {listProjects, refreshProject, selectProject} from './redux/actions';
import {bindActionCreators} from 'redux'
import {Collapse, Icon, Table} from 'antd';
import './ProjectsList.css';

const Panel = Collapse.Panel;
const Column = Table.Column;

export class ProjectsList extends Component {
  componentWillMount() {
    this.props.listProjects(this.props.pageNumber, this.props.pageSize);
  }

  handlePagination(page, pageSize) {
    this.props.listProjects(page - 1, pageSize);
  }

  refreshProject(projectId) {
    this.props.refreshProject(projectId);
  }

  selectProject(projectId) {
    this.props.selectProject(projectId);
  }

  render() {

    const pagination = {
      total: this.props.totalItems,
      pageSize: this.props.pageSize,
      current: this.props.pageNumber + 1,
      onChange: this.handlePagination.bind(this)
    };

    return (
        <div>
          <Collapse defaultActiveKey={['projects']}>
            <Panel header={this.props.title} key="projects">
              <Table dataSource={this.props.projects}
                     rowKey={record => record.projectId}
                     loading={this.props.loading} className={'projects'}
                     pagination={pagination}>
                <Column
                    title="Name"
                    dataIndex="name"
                    key="name"
                    width="40%"
                />
                <Column
                    title="Branch"
                    dataIndex="branch"
                    key="branch"
                    width="20%"
                />
                <Column
                    title="Last updated"
                    dataIndex="lastCollectDate"
                    key="lastCollectDate"
                    width="20%"
                    render={(text) => (
                        <FormattedTime
                            value={text}
                            day="numeric"
                            month="numeric"
                            year="numeric"/>
                    )}/>
                <Column
                    title="Actions"
                    key="action"
                    width="20%"
                    render={(text, record) => (
                        <span>
                          <a href={record.scmRepository.url} target={"_blank"}>
                            <Icon type="github" className={'action-btn'}/>
                          </a>
                          <a href={"#"}
                             onClick={this.refreshProject.bind(this,
                                 record.projectId)}
                             title={"Refresh"}>
                            <Icon type="reload" className={'action-btn'}/>
                          </a>
                          <a href={"#"}
                             onClick={this.selectProject.bind(this,
                                 record.projectId)}
                             title={"View descriptors"}>
                            <Icon type="plus-circle-o"
                                  className={'action-btn'}/>
                          </a>
                        </span>
                    )}/>
              </Table>
            </Panel>
          </Collapse>
        </div>
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
    totalPages: state.projects.totalPages,
    totalItems: state.projects.totalItems,
    loading: state.projects.loading
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators({listProjects, selectProject, refreshProject},
      dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(ProjectsList)