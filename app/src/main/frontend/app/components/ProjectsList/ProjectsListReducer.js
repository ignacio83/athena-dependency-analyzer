import {
  RECEIVE_PROJECTS,
  RECEIVE_PROJECTS_COUNT,
  RECEIVE_REFRESH_PROJECT,
  REQUEST_PROJECTS,
  REQUEST_REFRESH_PROJECT,
  REQUEST_REFRESH_PROJECT_FAILED,
  SELECT_PROJECT
} from './ProjectsListActions'

const initialState = {
  list: [],
  pageNumber: 0,
  pageSize: 10,
  search: {
    onlyWithDependencyManager: true
  },
  loading: false,
  refreshLoading: false,
  refreshError: false,
  refreshMessage: null,
  selected: null
};

export default function reducer(state = initialState, action) {
  switch (action.type) {
    case SELECT_PROJECT:
      return Object.assign({}, state, {
        loading: false,
        selected: action.project
      });
    case REQUEST_PROJECTS:
      return Object.assign({}, state, {
        loading: true,
        pageNumber: action.pageNumber,
        pageSize: action.pageSize,
        search: action.search
      });

    case RECEIVE_PROJECTS:
      return Object.assign({}, state, {
        loading: false,
        list: action.list,
        lastUpdated: action.receivedAt
      });

    case RECEIVE_PROJECTS_COUNT:
      return Object.assign({}, state, {
        loading: false,
        totalItems: action.totalItems,
        lastUpdated: action.receivedAt
      });

    case REQUEST_REFRESH_PROJECT:
      return Object.assign({}, state, {
        loading: true,
        refreshLoading: true,
        refreshError: false,
        refreshMessage: null
      });

    case RECEIVE_REFRESH_PROJECT:
      let refreshedProject = state.list.find(
          p => p.projectId === action.project.projectId);

      Object.assign(refreshedProject, action.project);

      return Object.assign({}, state, {
        loading: false,
        refreshLoading: false,
        selected: null,
        refreshError: false,
        refreshMessage: action.message
      });

    case REQUEST_REFRESH_PROJECT_FAILED:
      return Object.assign({}, state, {
        loading: false,
        refreshLoading: false,
        refreshError: true,
        refreshMessage: action.message
      });

    default:
      return state;
  }
}