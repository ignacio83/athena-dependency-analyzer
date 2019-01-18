import {
  CLOSE_ERROR_LOG,
  RECEIVE_ARTIFACTS,
  REQUEST_ARTIFACTS,
  VIEW_ERROR_LOG
} from './ArtifactsListActions'

const initialState = {
  list: [],
  loading: false
};

export default function reducer(state = initialState, action) {
  switch (action.type) {
    case REQUEST_ARTIFACTS:
      return Object.assign({}, state, {
        loading: true
      });

    case RECEIVE_ARTIFACTS:
      return Object.assign({}, state, {
        loading: false,
        list: action.list
      });
    case VIEW_ERROR_LOG:
      return Object.assign({}, state, {
        showErrorLogModal: true
      });

    case CLOSE_ERROR_LOG:
      return Object.assign({}, state, {
        showErrorLogModal: false
      });

    default:
      return state;
  }
}