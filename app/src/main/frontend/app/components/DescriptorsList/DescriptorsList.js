import React, {Component} from 'react';
import {connect} from 'react-redux';
import PropTypes from "prop-types";
import {
  closeDescriptorContent,
  listDescriptors,
  selectDescriptor,
  viewDescriptorContent
} from './DescriptorsListActions';
import {bindActionCreators} from 'redux'
import {Card, Icon, Modal, Table} from 'antd';
import './DescriptorsList.css';
import UnstableVersionIndicator
  from "../UnstableVersionIndicator/UnstableVersionIndicator";

const Column = Table.Column;

export class DescriptorsList extends Component {

  componentWillMount() {
    if (this.props.projectId) {
      this.props.listDescriptors(this.props.projectId);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.projectId && this.props.projectId !== nextProps.projectId) {
      this.props.listDescriptors(nextProps.projectId);
    }
  }

  onClickPlusIcon(descriptor, e) {
    this.props.selectDescriptor(descriptor);
    e.preventDefault();
  }

  onClickFileIcon(descriptorId, e) {
    this.props.viewDescriptorContent(this.props.projectId, descriptorId);
    e.preventDefault();
  }

  renderActions(text, record) {
    let result = [];
    result.push(<a href='#'
                   key="anchorPlusIcon"
                   onClick={this.onClickPlusIcon.bind(
                       this, record)}
                   title='View dependencies'>
      <Icon type='plus-circle-o' className='action-btn'/>
    </a>);

    if (record.contentStored) {
      result.push(<a href='#'
                     key="anchorFileIcon"
                     onClick={this.onClickFileIcon.bind(
                         this,
                         record.id)}
                     title='View descriptor'>
        <Icon type="file-text" className='action-btn'/>
      </a>);
    }
    return (<span key="actionSpan">{result}</span>);
  }

  closeDescriptorContent() {
    this.props.closeDescriptorContent();
  }

  render() {
    return (this.props.descriptors && this.props.descriptors.length > 0 &&
        <Card title={this.props.title}>
          <Table dataSource={this.props.descriptors}
                 rowKey={record => record.id}
                 loading={this.props.loading} className={'descriptors'}
                 title={() => "Project: " + this.props.project.name}
                 size="small">
            <Column
                title="Group Id"
                dataIndex="project.groupId"
                key="project.groupId"
                width="38%"
            />
            <Column
                title="Artifact Id"
                dataIndex="project.artifactId"
                key="project.artifactId"
                width="30%"
                render={(text, record) => {
                  let result = [];
                  if (record.lastExecution && record.lastExecution.fallback) {
                    result.push(<Icon key="ief" type="close-circle"
                                      theme="filled"
                                      className="icon-execution-fallback"
                                      title="A fallback was executed in the last analyze"/>);
                    result.push(" ");
                  }
                  result.push(text);
                  result.push(" ");
                  if (record.unstableArtifacts
                      && record.unstableArtifacts.length > 0) {
                    result.push(<UnstableVersionIndicator
                        artifacts={record.unstableArtifacts}
                        key={"u-" + record.id}/>);
                  }
                  return (<span>{result}</span>);
                }}
            />
            <Column
                title="Version"
                dataIndex="project.version"
                key="project.version"
                width="20%"/>
            <Column
                title="Actions"
                key="action"
                width="12%"
                render={this.renderActions.bind(this)}/>
          </Table>
          <Modal title="pom.xml"
                 visible={this.props.showDescriptorContentModal}
                 onOk={this.closeDescriptorContent.bind(this)}
                 onCancel={this.closeDescriptorContent.bind(this)}
                 width="50%"
                 className="descriptor-content"
                 footer={null}>
            <pre>
              {this.props.descriptorContent}
            </pre>
          </Modal>
        </Card>
    );

  }
}

DescriptorsList.propTypes = {
  title: PropTypes.string
};

const mapStateToProps = (state) => {
  return {
    project: state.projects.selected,
    projectId: state.projects.selected != null
        ? state.projects.selected.projectId : null,
    descriptors: state.projects.selected ? state.descriptors.list : [],
    descriptorContent: state.descriptors.descriptorContent,
    showDescriptorContentModal: state.descriptors.showDescriptorContentModal,
    loading: state.descriptors.loading
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators(
      {
        listDescriptors,
        selectDescriptor,
        viewDescriptorContent,
        closeDescriptorContent
      }, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(DescriptorsList)