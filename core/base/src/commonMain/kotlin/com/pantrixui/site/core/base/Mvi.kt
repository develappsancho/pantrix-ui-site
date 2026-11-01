package com.pantrixui.site.core.base

/**
 * Everything a screen draws, at one moment, as one immutable value. A screen is a function of its state: the same
 * state draws the same screen.
 */
interface UiState

/** Something the user did on a screen, or something the screen asks for: what a ViewModel is told. */
interface UiAction

/**
 * Something that happens once and is not part of what is drawn: opening another page, copying a text, opening a web
 * address. A ViewModel sends it; the screen acts on it.
 */
interface UiEffect
