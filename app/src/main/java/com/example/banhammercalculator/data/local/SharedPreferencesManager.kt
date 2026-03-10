package com.example.banhammercalculator.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Gerencia o armazenamento persistente de dados usando SharedPreferences.
 * Esta classe é responsável por salvar e recuperar o estado de banimento do usuário.
 */
class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "BanHammerCalculatorPrefs"
        private const val KEY_IS_BANNED = "is_banned"
    }

    /**
     * Salva o estado de banimento do usuário.
     * @param isBanned true se o usuário deve ser banido, false caso contrário.
     */
    fun setIsBanned(isBanned: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_BANNED, isBanned).apply()
    }

    /**
     * Recupera o estado de banimento do usuário.
     * @return true se o usuário está banido, false caso contrário (padrão).
     */
    fun getIsBanned(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BANNED, false)
    }

    /**
     * Limpa todos os dados armazenados nas SharedPreferences.
     * Útil para resetar o estado de banimento para fins de teste ou desenvolvimento.
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}