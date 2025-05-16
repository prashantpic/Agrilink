import { TypographyOptions } from '@mui/material/styles/createTypography';

const typography: TypographyOptions = {
  fontFamily: [
    'Roboto',
    '-apple-system',
    'BlinkMacSystemFont',
    '"Segoe UI"',
    '"Helvetica Neue"',
    'Arial',
    'sans-serif',
    '"Apple Color Emoji"',
    '"Segoe UI Emoji"',
    '"Segoe UI Symbol"',
  ].join(','),
  h1: {
    fontWeight: 300,
    fontSize: '6rem', // 96px
    lineHeight: 1.167,
    letterSpacing: '-0.01562em',
  },
  h2: {
    fontWeight: 300,
    fontSize: '3.75rem', // 60px
    lineHeight: 1.2,
    letterSpacing: '-0.00833em',
  },
  h3: {
    fontWeight: 400,
    fontSize: '3rem', // 48px
    lineHeight: 1.167,
    letterSpacing: '0em',
  },
  h4: {
    fontWeight: 400,
    fontSize: '2.125rem', // 34px
    lineHeight: 1.235,
    letterSpacing: '0.00735em',
  },
  h5: {
    fontWeight: 400,
    fontSize: '1.5rem', // 24px
    lineHeight: 1.334,
    letterSpacing: '0em',
  },
  h6: {
    fontWeight: 500,
    fontSize: '1.25rem', // 20px
    lineHeight: 1.6,
    letterSpacing: '0.0075em',
  },
  subtitle1: {
    fontWeight: 400,
    fontSize: '1rem', // 16px
    lineHeight: 1.75,
    letterSpacing: '0.00938em',
  },
  subtitle2: {
    fontWeight: 500,
    fontSize: '0.875rem', // 14px
    lineHeight: 1.57,
    letterSpacing: '0.00714em',
  },
  body1: {
    fontWeight: 400,
    fontSize: '1rem', // 16px
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  body2: {
    fontWeight: 400,
    fontSize: '0.875rem', // 14px
    lineHeight: 1.43,
    letterSpacing: '0.01071em',
  },
  button: {
    fontWeight: 500,
    fontSize: '0.875rem', // 14px
    lineHeight: 1.75,
    letterSpacing: '0.02857em',
    textTransform: 'uppercase',
  },
  caption: {
    fontWeight: 400,
    fontSize: '0.75rem', // 12px
    lineHeight: 1.66,
    letterSpacing: '0.03333em',
  },
  overline: {
    fontWeight: 400,
    fontSize: '0.75rem', // 12px
    lineHeight: 2.66,
    letterSpacing: '0.08333em',
    textTransform: 'uppercase',
  },
  // You can add custom variants if needed
  // customVariant: {
  //   fontFamily: 'YourCustomFont, Arial, sans-serif',
  //   fontWeight: 600,
  //   fontSize: '1.1rem',
  // }
};

export default typography;