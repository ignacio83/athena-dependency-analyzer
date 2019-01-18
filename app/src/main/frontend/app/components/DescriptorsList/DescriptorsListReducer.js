import {
  CLOSE_DESCRIPTOR_CONTENT,
  RECEIVE_DESCRIPTOR_CONTENT,
  RECEIVE_DESCRIPTORS,
  REQUEST_DESCRIPTOR_CONTENT,
  REQUEST_DESCRIPTORS,
  SELECT_DESCRIPTOR
} from './DescriptorsListActions'

const initialState = {
  list: [],
  loading: false,
  selected: null
};

export default function reducer(state = initialState, action) {
  switch (action.type) {
    case REQUEST_DESCRIPTORS:
      return Object.assign({}, state, {
        loading: true,
        selected: null
      });

    case RECEIVE_DESCRIPTORS:
      return Object.assign({}, state, {
        loading: false,
        list: action.list
      });
    case SELECT_DESCRIPTOR:
      return Object.assign({}, state, {
        loading: false,
        selected: action.descriptor
      });
    case REQUEST_DESCRIPTOR_CONTENT:
      return Object.assign({}, state, {
        loading: true
      });
    case RECEIVE_DESCRIPTOR_CONTENT:
      return Object.assign({}, state, {
        loading: false,
        descriptorContent: action.descriptorContent,
        showDescriptorContentModal: true
      });
    case CLOSE_DESCRIPTOR_CONTENT:
      return Object.assign({}, state, {
        descriptorContent: null,
        showDescriptorContentModal: false
      });
    default:
      return state;
  }
}