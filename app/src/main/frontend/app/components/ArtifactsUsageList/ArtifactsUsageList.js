import React, {Component} from 'react';
import PropTypes from "prop-types";
import {
  closeProjects,
  listArtifactsUsage,
  listProjectsByArtifact
} from "./ArtifactsUsageListActions";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {Button, Card, Divider, Form, Icon, Input, Table} from 'antd';
import UnstableVersionIndicator
  from "../UnstableVersionIndicator/UnstableVersionIndicator";
import Modal from "../../../node_modules/antd/lib/modal/Modal";

const Column = Table.Column;
const FormItem = Form.Item;

export class ArtifactsUsageList extends Component {

  componentWillMount() {
    this.props.listArtifactsUsage(this.props.pageNumber, this.props.pageSize,
        this.props.search);
  }

  handlePagination(page) {
    this.props.listArtifactsUsage(page - 1, this.props.pageSize,
        this.props.search);
  }

  handleChange(event) {
    this.props.search[event.target.name] = event.target.value;
  }

  search() {
    this.props.listArtifactsUsage(0, this.props.pageSize, this.props.search);
  }

  listProjectsByArtifact(groupId, artifactId, version) {
    this.props.listProjectsByArtifact(groupId, artifactId, version);
  }

  closeProjects() {
    this.props.closeProjects();
  }

  render() {
    const pagination = {
      total: this.props.totalItems,
      showTotal: total => 'Total of ' + total + ' artifacts',
      pageSize: this.props.pageSize,
      current: this.props.pageNumber + 1,
      onChange: this.handlePagination.bind(this)
    };

    return (
        <Card title={this.props.title}>
          <Form layout="inline">
            <FormItem label="Group Id">
              <Input
                  name="groupId"
                  placeholder="e.g. org.projectlombok"
                  onChange={this.handleChange.bind(this)}
                  style={{width: 300}}/>
            </FormItem>
            <FormItem label="Artifact Id">
              <Input name="artifactId" placeholder="e.g. lombok"
                     onChange={this.handleChange.bind(this)}
                     style={{width: 300}}/>
            </FormItem>
            <FormItem label="Version">
              <Input name="version" placeholder="e.g. 1.16.4"
                     onChange={this.handleChange.bind(this)}
                     style={{width: 100}}/>
            </FormItem>
            <FormItem>
              <Button type="primary" htmlType="submit"
                      onClick={this.search.bind(this)}>
                <Icon type="search"></Icon>
              </Button>
            </FormItem>
          </Form>
          <Divider/>
          <Table dataSource={this.props.artifacts}
                 rowKey={record => record.id}
                 loading={this.props.loading} className={'artifactsUsage'}
                 pagination={pagination}
                 size="small">
            <Column
                title="Group Id"
                dataIndex="groupId"
                key="groupId"
                width="40%"
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
                  }
                  else {
                    result.push(text);
                  }
                  return result;
                }}
            />
            <Column
                title="Projects"
                dataIndex="qtyProjects"
                key="qtyProjects"
                width="10%"
                render={(text, record) => {
                  let result = [];
                  result.push(text);
                  result.push(" ");
                  result.push(<a
                      key={record.groupId + ":" + record.artifactId + ":"
                      + record.version} href="#/artifactsUsage"
                      onClick={this.listProjectsByArtifact.bind(this,
                          record.groupId, record.artifactId,
                          record.version)}><Icon type="table"/></a>);
                  return result;
                }}
            />
          </Table>
          <Modal
              title={"Projects of "
              + this.props.selectedArtifactId + ":"
              + this.props.selectedVersion}
              visible={this.props.showProjectsModal}
              onOk={this.closeProjects.bind(this)}
              onCancel={this.closeProjects.bind(this)}
              footer={null}
          >
            <Table dataSource={this.props.projectsList} size="small">
              <Column
                  title="Name"
                  dataIndex="name"
                  key="name"
                  width="60%"
              />
              <Column
                  title="Branch"
                  dataIndex="branch"
                  key="branch"
                  width="40%"
              />
            </Table>
          </Modal>
        </Card>
    )
  }
}

ArtifactsUsageList.propTypes = {
  title: PropTypes.string
};

const mapStateToProps = (state) => {
  return {
    artifacts: state.artifactsUsage.list,
    pageSize: state.artifactsUsage.pageSize,
    pageNumber: state.artifactsUsage.pageNumber,
    loading: state.artifactsUsage.loading,
    search: state.artifactsUsage.search,
    totalItems: state.artifactsUsage.totalItems,
    selectedGroupId: state.artifactsUsage.selectedGroupId,
    selectedArtifactId: state.artifactsUsage.selectedArtifactId,
    selectedVersion: state.artifactsUsage.selectedVersion,
    projectsList: state.artifactsUsage.projectsList,
    showProjectsModal: state.artifactsUsage.showProjectsModal
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators({
    listArtifactsUsage: listArtifactsUsage,
    listProjectsByArtifact: listProjectsByArtifact,
    closeProjects: closeProjects
  }, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(ArtifactsUsageList)