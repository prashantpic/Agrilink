// src/types/leaflet.d.ts

// This file can be used to augment Leaflet's default types or declare types for Leaflet plugins
// that might not have their own @types packages or need specific project-level type adjustments.

// Example: If using a Leaflet plugin that adds new methods or options
/*
import * as L from 'leaflet';

declare module 'leaflet' {
  namespace SomePlugin {
    interface SomePluginOptions extends L.ControlOptions {
      option1?: string;
    }
    class SomePluginControl extends L.Control {
      constructor(options?: SomePluginOptions);
      // Add methods specific to the plugin
      customMethod(): void;
    }
  }

  namespace control {
    function somePlugin(options?: SomePlugin.SomePluginOptions): SomePlugin.SomePluginControl;
  }

  interface MapOptions {
    somePluginControl?: boolean;
  }
}
*/

// If no specific augmentations are needed immediately, this file can remain empty or with comments.
export {}; // Ensures this file is treated as a module.