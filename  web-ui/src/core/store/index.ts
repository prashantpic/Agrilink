import { configureStore, ThunkAction, Action } from '@reduxjs/toolkit';
import rootReducer from './rootReducer';
// import logger from 'redux-logger'; // Optional: for development debugging

const store = configureStore({
  reducer: rootReducer,
  middleware: (getDefaultMiddleware) => {
    const middleware = getDefaultMiddleware();
    // if (process.env.NODE_ENV === 'development') {
    //   middleware.push(logger); // Add logger only in development
    // }
    return middleware;
  },
  devTools: process.env.NODE_ENV !== 'production', // Enable Redux DevTools only in development
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<
  ReturnType,
  RootState,
  unknown,
  Action<string>
>;

export { store };