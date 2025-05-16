import { createTheme, responsiveFontSizes } from '@mui/material/styles';
import palette from './palette';
import typography from './typography';
// You can also import and merge component overrides here
// import componentOverrides from './componentOverrides';

let theme = createTheme({
  palette: palette,
  typography: typography,
  // components: componentOverrides, // If you have component-specific overrides
  // spacing: 8, // Default spacing factor
  // shape: {
  //   borderRadius: 8, // Default border radius
  // },
});

// Apply responsive font sizes
theme = responsiveFontSizes(theme);

export default theme;