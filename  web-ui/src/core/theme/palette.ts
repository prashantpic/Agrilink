import { PaletteOptions } from '@mui/material/styles';

const palette: PaletteOptions = {
  primary: {
    main: '#4CAF50', // Green - common for agriculture
    light: '#81C784',
    dark: '#388E3C',
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#FF9800', // Orange - for accents, warnings, or calls to action
    light: '#FFB74D',
    dark: '#F57C00',
    contrastText: '#000000',
  },
  error: {
    main: '#D32F2F', // Standard Red
    light: '#E57373',
    dark: '#C62828',
    contrastText: '#FFFFFF',
  },
  warning: {
    main: '#FFA000', // Amber
    light: '#FFC107',
    dark: '#FF8F00',
    contrastText: 'rgba(0, 0, 0, 0.87)',
  },
  info: {
    main: '#1976D2', // Standard Blue
    light: '#64B5F6',
    dark: '#1565C0',
    contrastText: '#FFFFFF',
  },
  success: {
    main: '#2E7D32', // Darker Green for success
    light: '#4CAF50',
    dark: '#1B5E20',
    contrastText: '#FFFFFF',
  },
  background: {
    default: '#F4F6F8', // Light grey for background
    paper: '#FFFFFF',   // White for paper elements like cards
  },
  text: {
    primary: 'rgba(0, 0, 0, 0.87)',
    secondary: 'rgba(0, 0, 0, 0.6)',
    disabled: 'rgba(0, 0, 0, 0.38)',
  },
  divider: 'rgba(0, 0, 0, 0.12)',
  // action: {
  //   active: 'rgba(0, 0, 0, 0.54)',
  //   hover: 'rgba(0, 0, 0, 0.04)',
  //   selected: 'rgba(0, 0, 0, 0.08)',
  //   disabled: 'rgba(0, 0, 0, 0.26)',
  //   disabledBackground: 'rgba(0, 0, 0, 0.12)',
  // },
};

export default palette;