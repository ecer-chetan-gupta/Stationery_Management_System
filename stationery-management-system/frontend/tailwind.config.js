/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f5ff',
          100: '#e0ebff',
          200: '#c7dcff',
          300: '#a3c4ff',
          400: '#75a2ff',
          500: '#4375ff', // custom vibrant blue
          600: '#264cff',
          700: '#1531ff',
          800: '#0b1ee6',
          900: '#0610bf',
        },
      },
    },
  },
  plugins: [],
}
