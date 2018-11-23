import React, {Component} from 'react';
import {FormattedNumber, FormattedTime} from "react-intl";
import {connect} from 'react-redux'
import PropTypes from "prop-types";
import {getRateLimit} from './RateLimitActions';
import {bindActionCreators} from 'redux'
import {Card, Col, Row, Spin} from "antd";

export class RateLimitCard extends Component {
  componentWillMount() {
    this.props.getRateLimit();
  }

  render() {
    let core, search, graphql;
    let resetCoreFormatted,
        resetSearchFormatted, resetGraphqlFormatted;
    let percentageUsedCoreFormatted, percentageUsedSearchFormatted,
        percentageUsedGraphqlFormatted;
    let configuredLimitPercentageCoreFormatted,
        configuredLimitPercentageSearchFormatted,
        configuredLimitPercentageGraphqlFormatted;

    if (this.props.rateLimit) {
      core = this.props.rateLimit.core;
      if (core) {
        resetCoreFormatted = <FormattedTime value={core.reset}/>;
        configuredLimitPercentageCoreFormatted =
            <FormattedNumber value={core.configuredLimitPercentage}
                             style="percent"
                             maximumFractionDigits={2}/>;
        percentageUsedCoreFormatted =
            <FormattedNumber value={core.percentageUsed} style="percent"
                             maximumFractionDigits={2}/>;
      }
      search = this.props.rateLimit.search;
      if (search) {
        resetSearchFormatted = <FormattedTime value={search.reset}/>;
        configuredLimitPercentageSearchFormatted =
            <FormattedNumber value={search.configuredLimitPercentage}
                             style="percent"
                             maximumFractionDigits={2}/>;
        percentageUsedSearchFormatted =
            <FormattedNumber value={search.percentageUsed} style="percent"
                             maximumFractionDigits={2}/>;
      }
      graphql = this.props.rateLimit.graphql;
      if (graphql) {
        resetGraphqlFormatted = <FormattedTime value={graphql.reset}/>;
        configuredLimitPercentageGraphqlFormatted =
            <FormattedNumber value={graphql.configuredLimitPercentage}
                             style="percent"
                             maximumFractionDigits={2}/>;
        percentageUsedGraphqlFormatted =
            <FormattedNumber value={graphql.percentageUsed} style="percent"
                             maximumFractionDigits={2}/>;
      }
    }

    return (
        <Card title={this.props.title} key="rateLimit">
          <Spin spinning={this.props.loading}>
            <Row type="flex" align="middle">
              <Col span={6}>
                <Row>
                  <h2>Core</h2>
                </Row>
                <Row>
                  <b>API limit:</b> {core && core.limit}
                </Row>
                <Row>
                  <b>Configured limit:</b>
                  {core
                  && core.configuredLimit} ({configuredLimitPercentageCoreFormatted})
                </Row>
                <Row>
                  <b>Remaining:</b> {core && core.remainingForConfiguredLimit.toString()
                } ({percentageUsedCoreFormatted} used)
                </Row>
                <Row>
                  <b>Will be reset at:</b> {resetCoreFormatted}
                </Row>
              </Col>
              <Col span={6}>
                <Row>
                  <h2>Search</h2>
                </Row>
                <Row>
                  <b>API limit:</b> {search && search.limit}
                </Row>
                <Row>
                  <b>Configured limit:</b> {search
                && search.configuredLimit} ({configuredLimitPercentageSearchFormatted})
                </Row>
                <Row>
                  <b>Remaining:</b> {search
                && search.remainingForConfiguredLimit} ({percentageUsedSearchFormatted} used)
                </Row>
                <Row>
                  <b>Will be reset at:</b> {resetSearchFormatted}
                </Row>
              </Col>
              <Col span={6}>
                <Row>
                  <h2>GraphQL</h2>
                </Row>
                <Row>
                  <b>API limit:</b> {graphql && graphql.limit}
                </Row>
                <Row>
                  <b>Configured limit:</b> {graphql
                && graphql.configuredLimit} ({configuredLimitPercentageGraphqlFormatted})
                </Row>
                <Row>
                  <b>Remaining:</b> {graphql
                && graphql.remainingForConfiguredLimit} ({percentageUsedGraphqlFormatted} used)
                </Row>
                <Row>
                  <b>Will be reset at:</b> {resetGraphqlFormatted}
                </Row>
              </Col>
            </Row>
          </Spin>
        </Card>
    )
  }
}

RateLimitCard.propTypes = {
  title: PropTypes.string
};

const mapStateToProps = (state) => {
  return {
    rateLimit: state.rateLimit.data,
    loading: state.rateLimit.loading
  }
};

function mapDispatchToProps(dispatch) {
  return bindActionCreators({getRateLimit}, dispatch);
}

export default connect(mapStateToProps, mapDispatchToProps)(RateLimitCard)