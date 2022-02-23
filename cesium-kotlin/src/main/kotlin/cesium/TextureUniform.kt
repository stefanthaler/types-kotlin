// Automatically generated - do not modify!

@file:JsModule("cesium")

package cesium

/**
 * A simple struct that serves as a value of a `sampler2D`-valued
 * uniform. This is used with [CustomShader] and [TextureManager]
 * @see <a href="https://cesium.com/docs/cesiumjs-ref-doc/TextureUniform.html">Online Documentation</a>
 *
 * @constructor
 * @param [textureMinificationFilter] The minification filter of the texture sampler.
 *   Default value - [TextureMinificationFilter.LINEAR]
 * @param [textureMagnificationFilter] The magnification filter of the texture sampler.
 *   Default value - [TextureMagnificationFilter.LINEAR]
 * @see <a href="https://cesium.com/docs/cesiumjs-ref-doc/TextureUniform.html">Online Documentation</a>
 */
external class TextureUniform(
    options: ConstructorOptions,
    textureMinificationFilter: TextureMinificationFilter? = definedExternally,
    textureMagnificationFilter: TextureMagnificationFilter? = definedExternally,
) {
    /**
     * @property [typedArray] A typed array storing the contents of a texture. Values are stored in row-major order. Since WebGL uses a y-up convention for textures, rows are listed from bottom to top.
     * @property [width] The width of the image. Required when options.typedArray is present
     * @property [height] The height of the image. Required when options.typedArray is present.
     * @property [url] A URL string or resource pointing to a texture image.
     * @property [repeat] When defined, the texture sampler will be set to wrap in both directions
     *   Default value - `true`
     * @property [pixelFormat] When options.typedArray is defined, this is used to determine the pixel format of the texture
     *   Default value - [PixelFormat.RGBA]
     * @property [pixelDatatype] When options.typedArray is defined, this is the data type of pixel values in the typed array.
     *   Default value - [PixelDatatype.UNSIGNED_BYTE]
     * @property [maximumAnisotropy] The maximum anisotropy of the texture sampler
     *   Default value - `1.0`
     */
    interface ConstructorOptions {
        var typedArray: org.khronos.webgl.Uint8Array?
        var width: Double?
        var height: Double?
        var url: Resource?
        var repeat: Boolean?
        var pixelFormat: PixelFormat?
        var pixelDatatype: PixelDatatype?
        var maximumAnisotropy: Double?
    }
}
