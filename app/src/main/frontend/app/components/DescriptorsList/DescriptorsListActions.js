import axios from 'axios'

export const REQUEST_DESCRIPTORS = 'REQUEST_DESCRIPTORS';
export const RECEIVE_DESCRIPTORS = 'RECEIVE_DESCRIPTOR';
export const SELECT_DESCRIPTOR = 'SELECT_DESCRIPTOR';
export const REQUEST_DESCRIPTOR_CONTENT = 'REQUEST_DESCRIPTOR_CONTENT';
export const RECEIVE_DESCRIPTOR_CONTENT = 'RECEIVE_DESCRIPTOR_CONTENT';
export const CLOSE_DESCRIPTOR_CONTENT = 'CLOSE_DESCRIPTOR_CONTENT_MODAL';

export function selectDescriptor(descriptor) {
  return {
    type: SELECT_DESCRIPTOR,
    descriptor: descriptor
  }
}

function requestDescriptors(projectId) {
  return {
    type: REQUEST_DESCRIPTORS,
    projectId: projectId
  }
}

function receiveDescriptors(data) {
  return {
    type: RECEIVE_DESCRIPTORS,
    list: data,
    receivedAt: Date.now()
  }
}

function requestDescriptorContent(projectId, descriptorId) {
  return {
    type: REQUEST_DESCRIPTOR_CONTENT,
    projectId: projectId,
    descriptorId: descriptorId
  }
}

function receiveDescriptorContent(content) {
  return {
    type: RECEIVE_DESCRIPTOR_CONTENT,
    descriptorContent: content,
    receivedAt: Date.now()
  }
}

export function listDescriptors(projectId) {
  return function (dispatch) {
    dispatch(requestDescriptors(projectId));
    axios.get('/api/v1/projects/' + projectId + '/descriptors').then(
        response => response,
        error => console.log('An error occurred.', error)
    ).then(response => dispatch(receiveDescriptors(response.data)));
  }
}

export function viewDescriptorContent(projectId, descriptorId) {
  return function (dispatch) {
    dispatch(requestDescriptorContent(projectId, descriptorId));
    axios.get(
        'api/v1/projects/' + projectId + '/descriptors/' + descriptorId
        + '/content').then(
        response => response,
        error => console.log('An error occurred.', error)
    ).then(response => dispatch(receiveDescriptorContent(response.data)));
  }
}

export function closeDescriptorContent() {
  return {
    type: CLOSE_DESCRIPTOR_CONTENT
  }
}