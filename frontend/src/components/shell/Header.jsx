import { NotificationsBell } from "./NotificationsBell";
import { UserMenu } from "./UserMenu";
import { EMERALD_GRADIENT_HORIZONTAL } from "./emeraldGradient";


export function Header() {
    return (
        <header
            className="sticky top-0 z-20 flex h-header shrink-0 items-center gap-2 border-b border-white/10 px-4 text-white backdrop-blur-glass sm:gap-4 sm:px-6"
            style={EMERALD_GRADIENT_HORIZONTAL}
        >
            <div className="min-w-0 flex-1 truncate text-base font-bold text-white sm:text-lg">
                <span className="sm:hidden">MicroLend</span>
                <span className="hidden sm:inline">
                    Microfinance &amp; NBFC Loan Management System
                </span>
            </div>

            <div className="flex shrink-0 items-center gap-1 sm:gap-2">
                <NotificationsBell />
                <UserMenu />
            </div>
        </header>
    );
}
