import React, {Component} from "react";
import {connect} from "react-redux";
import PropTypes from "prop-types";
import {Alert, Card, Icon, Modal, Table} from "antd";
import UnstableVersionIndicator
  from "../UnstableVersionIndicator/UnstableVersionIndicator";
import {bindActionCreators} from "redux";
import "./ArtifactsList.css"
import {
  closeErrorLog,
  listArtifacts,
  viewErrorLog
} from "./ArtifactsListActions";

const Column = Table.Column;

export class ArtifactsList extends Component {

  constructor(props) {
    super(props);
    this.normalizeAnalyzeType = this.normalizeAnalyzeType.bind(this);
  }

  componentWillMount() {
    if (this.props.descriptorId && this.props.projectId) {
      this.props.listArtifacts(this.props.projectId, this.props.descriptorId);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.descriptorId && nextProps.projectId
        && ((this.props.descriptorId !== nextProps.descriptorId)
            || (this.props.projectId !== nextProps.projectId))) {
      this.props.listArtifacts(nextProps.projectId, nextProps.descriptorId);
    }
  }

  closeErrorLog() {
    this.props.closeErrorLog();
  }

  onClickViewErrorLog(e) {
    this.props.viewErrorLog();
    e.preventDefault();
  }

  normalizeAnalyzeType(analyzeType) {
    return analyzeType === 'STATIC' ? "Static" : "Resolve dependencies";
  }

  render() {
    const groupIds = this.props.artifacts.map(
        artifact => artifact.groupId).filter(
        (value, index, self) => self.indexOf(value) === index);

    const scopes = this.props.artifacts.map(
        artifact => artifact.scope).filter(
        (value, index, self) => self.indexOf(value) === index);

    const groupIdColumnFilter = groupIds.map(groupId => {
      return {
        text: groupId,
        value: groupId
      }
    });

    const scopeColumnFilter = scopes.map(scope => {
      return {
        text: scope,
        value: scope
      }
    });

    let alert = [];
    if (this.props.descriptor && this.props.descriptor.lastExecution) {
      let lastExecution = this.props.descriptor.lastExecution;
      let analyzeTypeExecuted = lastExecution.analyzeTypeExecuted;
      let analyzeTypeRequested = lastExecution.analyzeTypeRequested;
      if (lastExecution.fallback) {
        let message = [];
        message.push(this.normalizeAnalyzeType(analyzeTypeRequested));
        message.push(" analyzes requested but was fallback to ");
        message.push(this.normalizeAnalyzeType(analyzeTypeExecuted));
        message.push(" analyzes. ");
        message.push(<a key="errorLogLink"
                        onClick={this.onClickViewErrorLog.bind(this)}>View error
          log</a>);
        message.push(<Modal key="modalErrorLog"
                            title={"Error log - "
                            + this.props.descriptor.name}
                            visible={this.props.showErrorLogModal}
                            onOk={this.closeErrorLog.bind(this)}
                            onCancel={this.closeErrorLog.bind(this)}
                            className="error-log"
                            width="50%"
                            footer={null}>
            <pre>
              {lastExecution.errorLog}
            </pre>
        </Modal>);

        alert.push(<Alert
            key="analyzeAlert"
            message={message}
            type="error" showIcon/>);
      } else {
        if (analyzeTypeExecuted === 'STATIC') {
          alert.push(<Alert
              key="analyzeAlert"
              message={this.normalizeAnalyzeType(analyzeTypeExecuted)
              + " analyze. Run again to resolved dependencies."}
              type="warning" showIcon/>);
        }
      }
    }

    return (this.props.artifacts && this.props.artifacts.length > 0 &&
        <Card
            title={this.props.title}>
          {alert}
          <Table dataSource={this.props.artifacts}
                 rowKey={record => record.id}
                 loading={this.props.loading} className={'artifacts'}
                 size="small" title={() => "Dependency Management Descriptor: "
              + this.props.descriptor.name}>
            <Column
                title="Group Id"
                dataIndex="groupId"
                key="groupId"
                width="30%"
                filters={groupIdColumnFilter}
                onFilter={(value, record) => record.groupId.indexOf(value)
                    === 0}
            />
            <Column
                title="Artifact Id"
                dataIndex="artifactId"
                key="artifactId"
                width="30%"
            />
            <Column
                title="Version"
                dataIndex="version"
                key="version"
                width="20%"
                render={(text, record) => {
                  let result = [];
                  if (record.report && !record.report.stable) {
                    result.push(<UnstableVersionIndicator key={record.id}
                                                          artifact={record}
                                                          version={text}>{text}</UnstableVersionIndicator>);
                  } else {
                    result.push(text);
                  }
                  return result;
                }}
            />
            <Column
                title="Scope"
                dataIndex="scope"
                key="scope"
                width="10%"
                filters={scopeColumnFilter}
                onFilter={(value, record) => record.scope.indexOf(value)
                    === 0}
            />
            <Column
                title="Origin"
                dataIndex="origin"
                key="origin"
                width="10%"
                render={(text, record) => {
                  let icon = 'glyphicon-question-sign';
                  switch (record.origin) {
                    case 'PARENT':
                      icon = 'arrow-up';
                      break;
                    case 'DEPENDENCIES_MANAGEMENT':
                      icon = 'tool';
                      break;
                    default:
                      icon = 'arrow-down';
                      break;
                  }
                  return (<Icon type={icon} className={'action-btn'}
                                title={record.origin}/>
                  );
                }}
            />
          </Table>
        </Card>
    )
  }
}

ArtifactsList.propTypes = {
  title: PropTypes.string
};

const mapStateToProps = (state) => {
  return {
    projectId: state.projects.selected != null
        ? state.projects.selected.projectId
        : null,
    descriptorId: state.descriptors.selected != null
        ? state.descriptors.selected.id
        : null,
    descriptor: state.descriptors.selected,
    artifacts: state.projects.selected && state.descriptors.selected
        ? state.artifacts.list : [],
    showErrorLogModal: state.artifacts.showErrorLogModal,
    loading: state.artifacts.loading
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators(
      {
        listArtifacts,
        viewErrorLog,
        closeErrorLog
      }, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(ArtifactsList)