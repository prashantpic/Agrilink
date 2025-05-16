// src/types/chartjs.d.ts

// This file can be used to augment Chart.js's default types or declare types for custom plugins.
// For example, if you create a custom Chart.js plugin, you might declare its options interface here.

// Example: Augmenting the ChartOptions type for a specific chart type if needed
/*
import { ChartOptions, ChartTypeRegistry } from 'chart.js';

declare module 'chart.js' {
  interface ChartOptions<TType extends keyof ChartTypeRegistry = keyof ChartTypeRegistry> {
    customOption?: string; // Example of adding a custom option
  }
}
*/

// If no specific augmentations are needed immediately, this file can remain empty or with comments.
// It's good practice to have it for future needs.
export {}; // Ensures this file is treated as a module.