/**
 * Vuetify3 Plugin
 */
// Vuetify
import * as directives from 'vuetify/directives';
import * as components from 'vuetify/components';
import { createVuetify } from 'vuetify';

// Styles
import 'vuetify/styles';

export default createVuetify({
  theme: {
    defaultTheme: 'dark'
  },
  // https://next.vuetifyjs.com/en/getting-started/installation/
  components,
  directives,
});
