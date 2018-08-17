import axios from 'axios'

export const SELECT_PROJECT = 'SELECT_PROJECT';
export const REQUEST_PROJECTS = 'REQUEST_PROJECTS';
export const RECEIVE_PROJECTS = 'RECEIVE_PROJECTS';
export const RECEIVE_PROJECTS_COUNT = 'RECEIVE_PROJECTS_COUNT';
export const REQUEST_REFRESH_PROJECT = 'REQUEST_REFRESH_PROJECT';
export const RECEIVE_REFRESH_PROJECT = 'RECEIVE_REFRESH_PROJECT';
export const REQUEST_REFRESH_PROJECT_FAILED = 'REQUEST_REFRESH_PROJECT_FAILED';

export function selectProject(projectId) {
  return {
    type: SELECT_PROJECT,
    projectId: projectId
  }
}

function requestProjects(pageNumber, pageSize, search) {
  return {
    type: REQUEST_PROJECTS,
    pageNumber: pageNumber,
    pageSize: pageSize,
    search: search
  }
}

function receiveProjects(data) {
  return {
    type: RECEIVE_PROJECTS,
    list: data,
    receivedAt: Date.now()
  }
}

function receiveProjectsCount(data) {
  return {
    type: RECEIVE_PROJECTS_COUNT,
    totalItems: data,
    receivedAt: Date.now()
  }
}

export function listProjects(pageNumber, pageSize, search) {
  return function (dispatch) {
    dispatch(requestProjects(pageNumber, pageSize, search));
    let queryString = 'pageNumber=' + pageNumber + '&pageSize=' + pageSize;
    if (search) {
      if (search.name) {
        queryString += '&name=' + search.name;
      }
      if (search.onlyWithDependencyManager) {
        queryString += '&onlyWithDependencyManager='
            + search.onlyWithDependencyManager;
      }
    }

    axios.get('/api/v1/projects?' + queryString)
    .then(response => dispatch(receiveProjects(response.data))).catch(
        error => console.log('An error occurred.', error));

    axios.get('/api/v1/projects/count?' + queryString)
    .then(response => dispatch(receiveProjectsCount(response.data))).catch(
        error => console.log('An error occurred.', error));
  }
}

function requestRefreshProject(projectId) {
  return {
    type: REQUEST_REFRESH_PROJECT,
    projectId: projectId
  }
}

function receiveRefreshProject(data) {
  return {
    type: RECEIVE_REFRESH_PROJECT,
    project: data,
    receivedAt: Date.now()
  }
}

function requestRefreshProjectFailed(error) {
  let errorMessage;
  if (error.response && error.response.data && error.response.data.message) {
    errorMessage = error.response.data.message;
    console.log('An error occurred.', errorMessage);
  }
  else {
    errorMessage = error.toString();
    console.log('An error occurred.', error);
  }
  return {
    type: REQUEST_REFRESH_PROJECT_FAILED,
    message: errorMessage
  }
}

export function refreshProject(projectId) {
  return function (dispatch) {
    dispatch(requestRefreshProject(projectId));
    axios.post('/api/v1/projects/' + projectId
        + '/refreshNow').then(
        response => dispatch(receiveRefreshProject(response.data))).catch(
        error => dispatch(requestRefreshProjectFailed(error)));
  }
}