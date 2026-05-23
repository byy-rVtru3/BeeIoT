import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import { fixupPluginRules } from '@eslint/compat';

import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import importPlugin from 'eslint-plugin-import';
import eslintConfigPrettier from 'eslint-config-prettier';

export default [
    {
        ignores: [
            '**/dist',
            '**/build',
            '**/public',
            '**/*.cjs',
            '**/node_modules',
            '**/.vscode',
            '**/.idea',
            '**/.github',
            '**/theme.ts',
        ],
    },
    js.configs.recommended,
    ...tseslint.configs.strict,
    ...tseslint.configs.stylistic,
    {
        files: ['src/**/*.tsx', 'src/**/*.ts'],
        languageOptions: {
            ecmaVersion: 2023,
            globals: globals.browser,
            parserOptions: {
                project: ['./tsconfig.app.json', './tsconfig.node.json'],
                tsconfigRootDir: import.meta.dirname,
            },
        },
        plugins: {
            react: fixupPluginRules(react),
            'react-hooks': fixupPluginRules(reactHooks),
            'jsx-a11y': fixupPluginRules(jsxA11y),
            import: fixupPluginRules(importPlugin),
            'react-refresh': reactRefresh,
        },
        settings: {
            react: { version: 'detect' },
            'import/resolver': {
                node: true,
                typescript: {
                    alwaysTryTypes: true,
                    project: ['./tsconfig.app.json', './tsconfig.node.json'],
                },
            },
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
            'react/jsx-uses-react': 'off',
            'react/react-in-jsx-scope': 'off',
            'react/prop-types': 'off',
            'react/display-name': 'off',
            'react/no-unknown-property': 'error',
            'react/self-closing-comp': 'error',
            'react-hooks/rules-of-hooks': 'error',
            'react-hooks/exhaustive-deps': 'warn',

            ...jsxA11y.configs.recommended.rules,

            'import/order': [
                'error',
                {
                    groups: ['builtin', 'external', 'internal', 'object', 'parent', 'sibling', 'index'],
                    alphabetize: { order: 'asc' },
                    'newlines-between': 'always',
                },
            ],

            '@typescript-eslint/consistent-type-definitions': ['error', 'type'],
            '@typescript-eslint/consistent-type-exports': 'error',
            '@typescript-eslint/consistent-type-imports': 'error',
            '@typescript-eslint/no-require-imports': 'error',
            '@typescript-eslint/no-useless-empty-export': 'error',
            '@typescript-eslint/no-unused-vars': 'error',
            '@typescript-eslint/no-unnecessary-boolean-literal-compare': 'warn',
            '@typescript-eslint/no-unnecessary-condition': 'warn',
            '@typescript-eslint/no-non-null-assertion': 'off',
            '@typescript-eslint/promise-function-async': ['error', { checkArrowFunctions: false }],

            'no-console': 'warn',
            'no-var': 'error',
            'prefer-const': 'error',
            curly: ['error', 'all'],
            'no-shadow-restricted-names': 'error',
        },
    },
    eslintConfigPrettier
];
