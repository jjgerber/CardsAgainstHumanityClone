/**
 * Vuetify3 Plugin
 */
// Vuetify
import * as directives from 'vuetify/directives';
import * as components from 'vuetify/components';
import { createVuetify } from 'vuetify';

// Styles
import 'vuetify/styles';
import colors from 'vuetify/lib/util/colors';

const cardsTheme = {
  dark: true,
  colors: {
    primary: colors.blue.base,
    secondary: colors.green.base
  }
}

export default createVuetify({
  theme: {
    defaultTheme: 'cardsTheme',
    themes: {
      cardsTheme
    }
  },
  components,
  directives,
});
