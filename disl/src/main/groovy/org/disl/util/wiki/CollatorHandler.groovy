/*
 * Copyright 2015 - 2023 GEM System a.s. <disl@gemsystem.cz>.
 *
 * This file is part of disl.
 *
 * Disl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Disl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Disl.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.disl.util.wiki

import java.text.Collator

/**
 * Collator for localized sorting.
 *
 * @author Lukáš Vlk
 */
class CollatorHandler {

    static Collator collator = null

    static Collator getCollator() {
        if (collator == null) {
            Locale.setDefault(new Locale("cs", "CZ"))
            collator = Collator.getInstance(Locale.getDefault())
        }
        return collator
    }

    static void setCollatorWithLocale(Locale locale) {
        Locale.setDefault(locale)
        collator = Collator.getInstance(locale)
    }
}
