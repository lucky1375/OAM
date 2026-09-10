module.exports = {
  devServer: {
    port: 8082,
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true
      }
    }
  },
  transpileDependencies: true
};
