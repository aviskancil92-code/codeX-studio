package com.example.core.model

data class LanguageDefinition(
    val id: String,
    val name: String,
    val extensions: List<String>,
    val runtime: String,
    val compiler: String? = null,
    val buildSystem: String,
    val packageManager: String? = null,
    val runner: String,
    val debugger: String? = null,
    val capabilities: CapabilityMatrix,
    val defaultFileName: String,
    val sampleCode: String,
    val monacoLanguage: String = id
)
