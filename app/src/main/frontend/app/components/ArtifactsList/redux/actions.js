import {RECEIVE_ARTIFACTS, REQUEST_ARTIFACTS} from './actionTypes'
import axios from 'axios'

function requestArtifacts(projectId, descriptorId) {
  return {
    type: REQUEST_ARTIFACTS,
    projectId: projectId,
    descriptorId: descriptorId
  }
}

function receiveArtifacts(data) {
  return {
    type: RECEIVE_ARTIFACTS,
    list: data.artifacts
  }
}

export function listArtifacts(projectId, descriptorId) {
  return function (dispatch) {
    dispatch(requestArtifacts(projectId, descriptorId));
    axios.get('/api/v1/projects/' + projectId
        + '/descriptors/' + descriptorId).then(
        response => response,
        error => console.log('An error occurred.', error)
    ).then(response => dispatch(receiveArtifacts(response.data)));
  }
}