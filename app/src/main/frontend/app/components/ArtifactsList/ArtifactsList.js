import React, {Component} from 'react';
import * as PropTypes from "react/lib/ReactPropTypes";
import {listArtifacts} from "./redux/actions";
import {bindActionCreators} from "redux";
import {connect} from "react-redux";
import {Collapse, Icon, Table} from 'antd';

const Panel = Collapse.Panel;
const Column = Table.Column;

export class ArtifactsList extends Component {

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

  render() {
    return ( this.props.artifacts && this.props.artifacts.length > 0 &&
        <Collapse defaultActiveKey={['artifacts']}>
          <Panel header={this.props.title} key="artifacts">
            <Table dataSource={this.props.artifacts}
                   rowKey={record => record.id}
                   loading={this.props.loading} className={'artifacts'}>
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
                  width="20%"/>
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
          </Panel>
        </Collapse>
    )
  }
}

ArtifactsList.propTypes = {
  title: PropTypes.string
};

const mapStateToProps = (state) => {
  return {
    projectId: state.projects.selectedId,
    descriptorId: state.descriptors.selectedId,
    artifacts: state.projects.selectedId && state.descriptors.selectedId
        ? state.artifacts.list : [],
    loading: state.artifacts.loading
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators({listArtifacts: listArtifacts}, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(ArtifactsList)