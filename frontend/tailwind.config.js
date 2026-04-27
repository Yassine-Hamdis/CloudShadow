/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary:   '#3f51b5',
        success:   '#4CAF50',
        warning:   '#FFC107',
        danger:    '#E53935',
        bg:        '#0f1724',
        card:      '#1f2937',
        'text-primary': '#E6EEF2',
        muted:     '#9AA6B2',
        border:    '#374151',
      },
      fontFamily: {
        inter: ['Inter', 'sans-serif'],
      },
    },
  },
  plugins: [],
}