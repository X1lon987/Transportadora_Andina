export default class Resumen{
    static #cv
    static async init(){
        Resumen.#cv = await Helpers.fetchText(`./resources/html/resume.html`)
        document.querySelector('main').innerHTML = Resumen.#cv
    }
}