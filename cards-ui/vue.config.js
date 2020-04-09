module.exports = {
  devServer: {
    port: '8081',
    proxy: {
      '/api': {
        target: 'http://localhost:8080/',
        changeOrigin: true,
        secure: false
      },
      '/socket': {
        target: 'http://localhost:8080/',
        changeOrigin: true,
        secure: false
      }
    }
  },
  publicPath: '/',
  outputDir: '../cards-backend/src/main/webapp/',
  assetsDir: '',
  transpileDependencies: [
    'vuetify'
  ]
}
