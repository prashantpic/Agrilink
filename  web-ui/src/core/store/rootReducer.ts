import { combineReducers } from '@reduxjs/toolkit';
import authReducer from '../../features/auth/store/authSlice';
// Import other slice reducers here as they are created
// e.g., import someFeatureReducer from '../../features/someFeature/someFeatureSlice';

const rootReducer = combineReducers({
  auth: authReducer,
  // someFeature: someFeatureReducer,
});

export type RootState = ReturnType<typeof rootReducer>;
export default rootReducer;