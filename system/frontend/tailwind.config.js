/** @type {import('tailwindcss').Config} */
import { addIconSelectors } from '@iconify/tailwind'

export default {
  darkMode: ['class', '.ceres-dark'],
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ceres: {
          bg: 'var(--ceres-bg)',
          'bg-soft': 'var(--ceres-bg-soft)',
          fg: 'var(--ceres-fg)',
          muted: 'var(--ceres-muted)',
          card: 'var(--ceres-card)',
          border: 'var(--ceres-border)',
          primary: 'rgb(var(--ceres-primary-rgb) / <alpha-value>)',
          'primary-solid': 'var(--ceres-primary)',
          'primary-hover': 'var(--ceres-primary-hover)',
          'primary-soft': 'var(--ceres-primary-soft)',
          indigo: 'var(--ceres-indigo)',
          cyan: 'var(--ceres-cyan)',
          emerald: 'var(--ceres-emerald)',
          warning: 'var(--ceres-warning)',
          danger: 'var(--ceres-danger)',
        },
      },
      borderRadius: {
        ceres: 'var(--ceres-radius)',
        'ceres-sm': 'var(--ceres-radius-sm)',
      },
      boxShadow: {
        ceres: 'var(--ceres-shadow)',
        'ceres-sm': 'var(--ceres-shadow-sm)',
        'ceres-lg': 'var(--ceres-shadow-lg)',
      },
      keyframes: {
        'ceres-fade-up': {
          '0%': { opacity: '0', transform: 'translateY(12px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
      animation: {
        'ceres-fade-up': 'ceres-fade-up 260ms ease-out both',
      },
    },
  },
  plugins: [addIconSelectors(['gg', 'ic', 'solar', 'eos-icons', 'carbon'])],
  corePlugins: {
    preflight: false,
  },
}
