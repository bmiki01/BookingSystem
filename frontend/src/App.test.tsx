import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import App from './App';

describe('App Component', () => {
  it('renders branding navigation logo', () => {
    render(<App />);
    expect(screen.getByText(/BookIt/i)).toBeInTheDocument();
  });
});
