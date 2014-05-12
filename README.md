# plovr

This is a fork of the plovr Closure build tool.
The Closure code is included as submodules to ensure up to date sources and the translation support is extended.

## Translations

To improve the translation support for plovr, the [XtbGenerator](https://github.com/kuzmisin/xtbgenerator) from kuzmisin was integrated.
You can extract your messages and specify translation files in the config.

### JS Usage
Use the default MSG syntax from the Closure Library in your JS files:

```javascript
/** @desc Description for Test 1 */
var MSG_TEST_1 = goog.getMsg('Test 1');
```

or use the msg statement in soy templates:

```
{msg desc="Says hello and tells user to click a link."}
  Hello {$userName}! Please click <a href="{$url}">here</a>.
{/msg}
```

### Config File
In your plovr config file you need to specify your supported languages and the translation files. When using the extract command the given xtb files will be generated/extended.

For compilation the `{LANG}` placeholder in your `"module-output-path"` is used to place the language specific versions into different destinations.

#### Example:
```
  "languages": {
    "de": "../asset/common/locales/de.xtb",
    "en": "../asset/common/locales/en.xtb"
  },

  ...,

  "module-output-path": "../asset/common/js-min/{LANG}/%s.js",
```

### Plovr Usage

#### Extract Messages
The extract command will search for MSG statements in your js and soy sources and generates the xtb files. Existing files will be extended and changed translation will not be overridden!

```
java -jar plovr.jar extract config.json
```

#### Build
When using the build command, plovr will compile an extra version of your code for each specified language. In addition the `goog.LOCALE` constant will be set.

```
java -jar plovr.jar build config.json
```

## Download
https://github.com/c-esswein/plovr/raw/master/bin/plovr.jar