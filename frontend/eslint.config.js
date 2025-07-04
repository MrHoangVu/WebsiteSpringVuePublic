import js from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import globals from "globals";
import pluginVitest from "@vitest/eslint-plugin";

export default [
  {
    name: "app/files-to-lint",
    files: ["**/*.{js,mjs,jsx,vue}"],
  },

  {
    name: "app/files-to-ignore",
    ignores: ["**/dist/**", "**/dist-ssr/**", "**/coverage/**"],
  },

  {
    languageOptions: {
      globals: {
        ...globals.browser,
      },
    },
  },

  js.configs.recommended,
  ...pluginVue.configs["flat/essential"],

  {
    ...pluginVitest.configs.recommended,
    files: ["src/**/__tests__/*"],
  },
  // .eslintrc.js (ví dụ)
  (module.exports = {
    extends: [
      "eslint:recommended",
      "plugin:vue/vue3-recommended", // hoặc vue3-essential, vue3-strongly-recommended
    ],
    rules: {
      // Thêm các quy tắc tùy chỉnh của bạn ở đây
    },
  }),
];
