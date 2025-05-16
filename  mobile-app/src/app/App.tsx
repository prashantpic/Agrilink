import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider } from '../core/auth/AuthContext'; // Assuming AuthContext will be created
import { NetworkProvider } from '../core/network/NetworkProvider'; // Assuming NetworkProvider will be created
import { SyncStatusProvider } from '../core/sync/SyncStatusProvider'; // Assuming SyncStatusProvider will be created
import AppNavigator from '../core/navigation/AppNavigator'; // Assuming AppNavigator will be created
// import { ThemeProvider } from 'your-theme-library'; // Optional: if a theme library is used

// Initialize WatermelonDB (this might be done elsewhere, e.g., before app mount, or db instance passed via context)
// import { initializeDatabase } from '../core/data/local/db';
// initializeDatabase();


const App = () => {
  // Potentially initialize DB or other services here if needed before rendering
  // For example, one-time setup for WatermelonDB could be triggered here or at a higher level if possible.

  return (
    <SafeAreaProvider>
      <AuthProvider>
        <NetworkProvider>
          <SyncStatusProvider>
            {/* <ThemeProvider theme={yourDefaultTheme}> */}
            <NavigationContainer>
              <AppNavigator />
            </NavigationContainer>
            {/* </ThemeProvider> */}
          </SyncStatusProvider>
        </NetworkProvider>
      </AuthProvider>
    </SafeAreaProvider>
  );
};

export default App;