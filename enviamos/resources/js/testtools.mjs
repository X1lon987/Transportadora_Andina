export default class TestTools {
  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use TestTools.init()')
  }

  static async init() {
    console.log('Iniciadas las pruebas de herramientas')

    // ejemplo de uso de los estándares definidos en index.mjs:
    let html = `
      <br>
      <a class="lead" href="https://es.javascript.info/browser-environment" target="_blank">
        <strong>Entorno del navegador, especificaciones<strong>
      </a>
      <br>
    `
    document.querySelector('main').insertAdjacentHTML('beforeend', html)
  }
}
