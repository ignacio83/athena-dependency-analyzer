import React, {Component} from 'react';
import {connect} from 'react-redux'
import {fullScan} from './FullScanButtonActions';
import {bindActionCreators} from 'redux'
import {Button, message, Tooltip} from 'antd';

export class FullScanButton extends Component {

  fullScan() {
    this.props.fullScan();
  }

  componentWillUpdate(nextProps) {
    if (this.props.loading && !nextProps.loading) {
      if (nextProps.error) {
        message.error(nextProps.errorMessage, 5);
      } else {
        message.success(
            "Scan requested for " + nextProps.projects.length
            + " projects... The scan is running in the background...");
      }
    }
  }

  render() {
    return (
        <Tooltip
            title="Scan all repositories of organization, collecting and analyzing dependencies"
            placement="bottom">
          <Button loading={this.props.loading}
                  onClick={this.fullScan.bind(this)}
                  icon="sync"/>
        </Tooltip>
    )
  }
}

const mapStateToProps = (state) => {
  return {
    loading: state.fullScanButton.loading,
    error: state.fullScanButton.error,
    errorMessage: state.fullScanButton.errorMessage,
    projects: state.fullScanButton.projects
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators({fullScan}, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(FullScanButton)