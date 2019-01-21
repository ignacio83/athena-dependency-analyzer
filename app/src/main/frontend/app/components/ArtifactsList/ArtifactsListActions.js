import axios from "axios"

export const REQUEST_ARTIFACTS = "REQUEST_ARTIFACTS";
export const RECEIVE_ARTIFACTS = "RECEIVE_ARTIFACTS";
export const VIEW_ERROR_LOG = "VIEW_ERROR_LOG";
export const CLOSE_ERROR_LOG = "CLOSE_ERROR_LOG";

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

export function viewErrorLog() {
  return {
    type: VIEW_ERROR_LOG
  }
}

export function closeErrorLog() {
  return {
    type: CLOSE_ERROR_LOG
  }
}

export function listArtifacts(projectId, descriptorId) {
  return function (dispatch) {
    dispatch(requestArtifacts(projectId, descriptorId));
    axios.get("/api/v1/projects/" + projectId
        + "/descriptors/" + descriptorId).then(
        response => response,
        error => console.log("An error occurred.", error)
    ).then(response => dispatch(receiveArtifacts(response.data)));
  }
}