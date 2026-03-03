import { Component, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterLink, ActivatedRoute } from "@angular/router";
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
} from "@angular/forms";

import { AccountService } from "../../../shared/services/account.service";
import { AdminAccountService } from "../../services/account.service";
import { BankingApiService } from "../../../core/services/banking-api.service";
import {
  Transaction,
  TransactionType,
  TransactionFilter,
  PagedResponse,
  Account,
} from "../../../shared/models/account.model";
import { AccountSelectionDTO } from "../../../shared/models/banking-dtos.model";
import { LoaderComponent } from "../../../shared/components/loader/loader.component";
import { InlineAlertComponent } from "../../../shared/components/inline-alert/inline-alert.component";
import { AdminCustomerService } from "../../services/customer.service";

@Component({
  selector: "app-admin-transactions",
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    ReactiveFormsModule,
    LoaderComponent,
    InlineAlertComponent,
  ],
  template: `
    <div class="container-fluid">
      <!-- Page Header -->
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 class="mb-1">Transaction Management</h2>
          <p class="text-muted mb-0">
            View all transactions and perform banking operations
          </p>
        </div>
        <div class="d-flex gap-2">
          <button
            class="btn btn-success"
            (click)="openOperationModal('credit')"
            [disabled]="loading"
          >
            <i class="bi bi-plus-circle me-2"></i>
            Credit
          </button>
          <button
            class="btn btn-warning"
            (click)="openOperationModal('debit')"
            [disabled]="loading"
          >
            <i class="bi bi-dash-circle me-2"></i>
            Debit
          </button>
          <button
            class="btn btn-primary"
            routerLink="/admin/transfer"
            [disabled]="loading"
          >
            <i class="bi bi-arrow-left-right me-2"></i>
            Transfer
          </button>
        </div>
      </div>

      <!-- Success/Error Messages -->
      <div
        *ngIf="successMessage"
        class="alert alert-success alert-dismissible fade show"
        role="alert"
      >
        {{ successMessage }}
        <button
          type="button"
          class="btn-close"
          (click)="successMessage = ''"
          aria-label="Close"
        ></button>
      </div>

      <div
        *ngIf="errorMessage"
        class="alert alert-danger alert-dismissible fade show"
        role="alert"
      >
        {{ errorMessage }}
        <button
          type="button"
          class="btn-close"
          (click)="errorMessage = ''"
          aria-label="Close"
        ></button>
      </div>

      <!-- Transactions Table -->
      <div class="card">
        <div
          class="card-header d-flex justify-content-between align-items-center"
        >
          <h5 class="card-title mb-0">All Transactions</h5>
          <div class="d-flex align-items-center gap-2">
            <span class="badge bg-primary">
              {{ pagedResponse?.totalElements || 0 }} total
            </span>
            <button
              class="btn btn-sm btn-outline-primary"
              (click)="applyFilters()"
              [disabled]="loading"
            >
              <i class="bi bi-arrow-clockwise me-1"></i>Refresh
            </button>
          </div>
        </div>
        <div class="card-body p-0">
          <app-loader *ngIf="loading"></app-loader>
          <app-inline-alert
            *ngIf="error && !loading"
            type="danger"
            [message]="error"
          ></app-inline-alert>

          <!-- Emergency fallback button -->
          <div *ngIf="error && !loading" class="text-center p-3">
            <button
              class="btn btn-outline-primary me-2"
              (click)="loadTransactionsWithoutFilters()"
            >
              <i class="bi bi-arrow-clockwise me-2"></i>
              Try Loading Without Filters
            </button>
            <button
              class="btn btn-outline-info"
              (click)="showBackendTroubleshooting()"
            >
              <i class="bi bi-info-circle me-2"></i>
              Backend Troubleshooting
            </button>
          </div>

          <div *ngIf="!loading && !error" class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
                <tr>
                  <th>ID</th>
                  <th>Customer</th>
                  <th>Account</th>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Description</th>
                  <th>Date</th>
                  <th>Balance</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let transaction of transactions">
                  <td>{{ transaction.id }}</td>
                  <td>
                    <div class="d-flex align-items-center">
                      <i class="bi bi-person-circle text-muted me-2"></i>
                      {{ getCustomerName(transaction) }}
                    </div>
                  </td>
                  <td>
                    <div class="d-flex flex-column">
                      <span class="fw-semibold">{{
                        getAccountDisplayName(transaction.accountId)
                      }}</span>
                      <small class="text-muted"
                        >ID: {{ transaction.accountId }}</small
                      >
                    </div>
                  </td>
                  <td>
                    <span
                      class="badge"
                      [class]="getTransactionTypeBadge(transaction.type)"
                    >
                      {{ transaction.type }}
                    </span>
                  </td>
                  <td
                    class="fw-semibold"
                    [class]="getAmountClass(transaction.type)"
                  >
                    {{ transaction.amount | currency }}
                  </td>
                  <td>{{ transaction.description || "N/A" }}</td>
                  <td>{{ transaction.operationDate | date: "short" }}</td>
                  <td>{{ transaction.accountBalance | currency }}</td>
                </tr>
                <tr *ngIf="transactions.length === 0">
                  <td colspan="8" class="text-center py-4 text-muted">
                    No transactions found
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Enhanced Pagination -->
          <div
            *ngIf="pagedResponse && pagedResponse.totalPages > 1"
            class="card-footer bg-white"
          >
            <div class="row align-items-center">
              <!-- Pagination Info -->
              <div class="col-md-6 mb-2 mb-md-0">
                <div class="pagination-info">
                  <small class="text-muted">
                    Showing {{ getStartRecord() }} to {{ getEndRecord() }} of
                    {{ pagedResponse.totalElements }} transactions
                  </small>
                </div>
              </div>

              <!-- Pagination Controls -->
              <div class="col-md-6">
                <nav aria-label="Transaction pagination">
                  <ul
                    class="pagination pagination-sm justify-content-md-end justify-content-center mb-0"
                  >
                    <!-- First Page -->
                    <li
                      class="page-item"
                      [class.disabled]="pagedResponse.first"
                    >
                      <button
                        class="page-link"
                        (click)="goToPage(0)"
                        [disabled]="pagedResponse.first"
                        title="First page"
                      >
                        <i class="bi bi-chevron-double-left"></i>
                      </button>
                    </li>

                    <!-- Previous Page -->
                    <li
                      class="page-item"
                      [class.disabled]="pagedResponse.first"
                    >
                      <button
                        class="page-link"
                        (click)="goToPage(pagedResponse.number - 1)"
                        [disabled]="pagedResponse.first"
                        title="Previous page"
                      >
                        <i class="bi bi-chevron-left"></i>
                      </button>
                    </li>

                    <!-- Page Numbers -->
                    <li
                      *ngFor="let page of getVisiblePages()"
                      class="page-item"
                      [class.active]="page === pagedResponse.number + 1"
                    >
                      <button
                        *ngIf="page !== '...'; else ellipsis"
                        class="page-link"
                        (click)="goToPage(+page - 1)"
                        [disabled]="page === pagedResponse.number + 1"
                      >
                        {{ page }}
                      </button>
                      <ng-template #ellipsis>
                        <span class="page-link">...</span>
                      </ng-template>
                    </li>

                    <!-- Next Page -->
                    <li class="page-item" [class.disabled]="pagedResponse.last">
                      <button
                        class="page-link"
                        (click)="goToPage(pagedResponse.number + 1)"
                        [disabled]="pagedResponse.last"
                        title="Next page"
                      >
                        <i class="bi bi-chevron-right"></i>
                      </button>
                    </li>

                    <!-- Last Page -->
                    <li class="page-item" [class.disabled]="pagedResponse.last">
                      <button
                        class="page-link"
                        (click)="goToPage(pagedResponse.totalPages - 1)"
                        [disabled]="pagedResponse.last"
                        title="Last page"
                      >
                        <i class="bi bi-chevron-double-right"></i>
                      </button>
                    </li>
                  </ul>
                </nav>
              </div>
            </div>

            <!-- Page Size Selector -->
            <div class="row mt-3">
              <div class="col-md-6">
                <div class="d-flex align-items-center">
                  <label class="form-label me-2 mb-0">Items per page:</label>
                  <select
                    class="form-select form-select-sm"
                    style="width: auto;"
                    [(ngModel)]="pageSize"
                    (change)="changePageSize()"
                  >
                    <option value="10">10</option>
                    <option value="20">20</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                  </select>
                </div>
              </div>

              <!-- Quick Jump -->
              <div class="col-md-6">
                <div class="d-flex align-items-center justify-content-md-end">
                  <label class="form-label me-2 mb-0">Go to page:</label>
                  <div class="input-group" style="width: 120px;">
                    <input
                      type="number"
                      class="form-control form-control-sm"
                      [value]="pagedResponse.number + 1"
                      (keyup.enter)="jumpToPage($event)"
                      [min]="1"
                      [max]="pagedResponse.totalPages"
                      placeholder="Page"
                    />
                    <button
                      class="btn btn-outline-secondary btn-sm"
                      type="button"
                      (click)="jumpToPage($event)"
                      title="Go to page"
                    >
                      <i class="bi bi-arrow-right"></i>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Banking Operation Modal -->
    <div
      *ngIf="showModal"
      class="modal d-block"
      tabindex="-1"
      style="background-color: rgba(0,0,0,0.5);"
    >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ getOperationTitle() }}
            </h5>
            <button
              type="button"
              class="btn-close"
              (click)="closeModal()"
              aria-label="Close"
            ></button>
          </div>
          <form [formGroup]="operationForm" (ngSubmit)="performOperation()">
            <div class="modal-body">
              <div *ngIf="errorMessage" class="alert alert-danger">
                {{ errorMessage }}
              </div>

              <div class="mb-3">
                <label for="accountId" class="form-label"
                  >Select Account *</label
                >
                <select
                  class="form-select"
                  id="accountId"
                  formControlName="accountId"
                >
                  <option value="">Choose an account...</option>
                  <option
                    *ngFor="let account of accountsForSelection"
                    [value]="account.accountId"
                  >
                    {{ account.customerUsername }} -
                    {{ account.customerName }} ({{ account.accountType }}:
                    {{
                      account.balance | currency: "USD" : "symbol" : "1.2-2"
                    }})
                  </option>
                </select>
                <div
                  *ngIf="
                    operationForm.get('accountId')?.invalid &&
                    operationForm.get('accountId')?.touched
                  "
                  class="text-danger small mt-1"
                >
                  Please select an account
                </div>
              </div>
              <div class="mb-3">
                <label for="amount" class="form-label">Amount *</label>
                <input
                  type="number"
                  class="form-control"
                  id="amount"
                  formControlName="amount"
                  placeholder="Enter amount"
                  min="0.01"
                  step="0.01"
                />
                <div
                  *ngIf="
                    operationForm.get('amount')?.invalid &&
                    operationForm.get('amount')?.touched
                  "
                  class="text-danger small mt-1"
                >
                  Valid amount is required
                </div>
              </div>
              <div class="mb-3">
                <label for="description" class="form-label"
                  >Description *</label
                >
                <input
                  type="text"
                  class="form-control"
                  id="description"
                  formControlName="description"
                  placeholder="Enter description"
                />
                <div
                  *ngIf="
                    operationForm.get('description')?.invalid &&
                    operationForm.get('description')?.touched
                  "
                  class="text-danger small mt-1"
                >
                  Description is required
                </div>
              </div>
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-secondary"
                (click)="closeModal()"
              >
                Cancel
              </button>
              <button
                type="submit"
                class="btn"
                [class]="getOperationButtonClass()"
                [disabled]="operationForm.invalid || operationLoading"
              >
                <span
                  *ngIf="operationLoading"
                  class="spinner-border spinner-border-sm me-2"
                ></span>
                {{ getOperationTitle() }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .table th {
        border-top: none;
        font-weight: 600;
        color: var(--bs-gray-700);
      }

      .badge {
        font-size: 0.75rem;
        padding: 0.375rem 0.75rem;
      }

      code {
        font-size: 0.875rem;
        color: var(--bs-gray-700);
        background-color: var(--bs-gray-100);
        padding: 0.25rem 0.5rem;
        border-radius: 0.25rem;
      }

      .pagination .page-link {
        color: var(--bs-primary);
      }

      .pagination .page-item.active .page-link {
        background-color: var(--bs-primary);
        border-color: var(--bs-primary);
      }
    `,
  ],
})
export class AdminTransactionsComponent implements OnInit {
  transactions: any[] = [];
  allTransactions: any[] = [];
  pagedResponse: PagedResponse<any> | null = null;
  loading = false;
  error = "";
  successMessage = "";
  errorMessage = "";
  accountsForSelection: AccountSelectionDTO[] = [];

  // Client-side pagination
  pageSize = 20;
  currentPage = 0;

  // Operation modal
  currentOperation: "credit" | "debit" = "credit";
  operationLoading = false;
  operationForm: FormGroup;
  showModal = false;

  filter: TransactionFilter = {
    page: 0,
    size: 500,
    sortBy: "operationDate",
    sortDirection: "desc" as "desc",
  };

  Math = Math;

  private customerIdFilter: number | null = null;
  private customerAccountIds: Set<string> | null = null;

  constructor(
    private accountService: AccountService,
    private adminAccountService: AdminAccountService,
    private bankingApiService: BankingApiService,
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private adminCustomerService: AdminCustomerService,
  ) {
    this.operationForm = this.fb.group({
      accountId: ["", [Validators.required]],
      amount: ["", [Validators.required, Validators.min(0.01)]],
      description: ["", [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe((params) => {
      const customerIdParam = params.get("customerId");
      this.customerIdFilter = customerIdParam ? Number(customerIdParam) : null;
      if (this.customerIdFilter != null) {
        this.loadCustomerAccountIds(this.customerIdFilter);
      } else {
        this.customerAccountIds = null;
      }
      this.loadTransactions();
      this.loadAccounts();
    });
  }

  private loadCustomerAccountIds(customerId: number): void {
    this.adminCustomerService.getCustomerAccounts(customerId).subscribe({
      next: (accounts) => {
        const ids =
          accounts?.map((a: any) => String(a.id ?? a.accountId ?? "").trim()) ??
          [];
        this.customerAccountIds = new Set(ids.filter((id) => !!id));
        this.applyFilters();
      },
      error: (err) => {
        console.error(
          "AdminTransactionsComponent.loadCustomerAccountIds() - Error:",
          err,
        );
        this.customerAccountIds = null;
      },
    });
  }

  loadAccounts(): void {
    this.bankingApiService.getAccountsForSelection().subscribe({
      next: (accounts: any[]) => {
        this.accountsForSelection = (accounts || []).map((a) => ({
          accountId: a.accountId ?? a.id,
          customerUsername: a.customerDTO?.name ?? a.customerUsername ?? "",
          customerName: a.customerDTO?.name ?? a.customerName ?? "",
          accountType: a.type ?? a.accountType ?? "Account",
          balance: a.balance ?? 0,
          status: (a.status ?? "ACTIVATED") as
            | "CREATED"
            | "ACTIVATED"
            | "SUSPENDED"
            | "BLOCKED",
        }));
      },
      error: (error) => {
        console.error("Error loading accounts for selection:", error);
      },
    });
  }

  loadTransactions(): void {
    this.loading = true;
    this.error = "";

    const fetchFilter: TransactionFilter = {
      page: 0,
      size: 500,
      sortBy: this.filter.sortBy || "operationDate",
      sortDirection: (this.filter.sortDirection as "desc") || "desc",
    };

    this.accountService.getTransactions(fetchFilter).subscribe({
      next: (response) => {
        this.allTransactions = response.content ?? [];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error(
          "AdminTransactionsComponent.loadTransactions() - Error:",
          err,
        );
        this.error =
          err.status === 500
            ? "Server error while loading transactions. Please try again."
            : err.status === 400
              ? "Invalid request. Please check your connection."
              : "Failed to load transactions. Please try again.";
        this.loading = false;
      },
    });
  }

  applyFilters(): void {
    this.currentPage = 0;
    let list = [...this.allTransactions];

    // If we are in a specific customer context, keep only that customer's accounts
    if (this.customerAccountIds && this.customerAccountIds.size > 0) {
      list = list.filter((t) =>
        this.customerAccountIds!.has(
          String(t.accountId ?? t.bankAccountId ?? "").trim(),
        ),
      );
    }

    const accountId =
      this.filter.accountId != null && this.filter.accountId !== ""
        ? String(this.filter.accountId).trim()
        : "";
    if (accountId) {
      list = list.filter(
        (t) => (t.accountId ?? t.bankAccountId ?? "") === accountId,
      );
    }

    const typeFilter = (this.filter.type ?? "").toString().trim().toUpperCase();
    if (typeFilter) {
      list = list.filter((t) => {
        const tType = (t.type ?? "").toString().toUpperCase();
        if (typeFilter === "DEPOSIT")
          return tType === "DEPOSIT" || tType === "CREDIT";
        if (typeFilter === "WITHDRAWAL")
          return tType === "WITHDRAWAL" || tType === "DEBIT";
        return tType === typeFilter;
      });
    }

    const startDate = (this.filter.startDate ?? "").toString().trim();
    if (startDate) {
      const start = new Date(startDate);
      start.setHours(0, 0, 0, 0);
      list = list.filter((t) => {
        const d = t.operationDate ? new Date(t.operationDate) : null;
        return d && d >= start;
      });
    }

    const endDate = (this.filter.endDate ?? "").toString().trim();
    if (endDate) {
      const end = new Date(endDate);
      end.setHours(23, 59, 59, 999);
      list = list.filter((t) => {
        const d = t.operationDate ? new Date(t.operationDate) : null;
        return d && d <= end;
      });
    }

    const totalElements = list.length;
    const size = Number(this.pageSize) || 20;
    const totalPages = Math.max(1, Math.ceil(totalElements / size));
    if (this.currentPage >= totalPages)
      this.currentPage = Math.max(0, totalPages - 1);
    const start = this.currentPage * size;
    this.transactions = list.slice(start, start + size);

    this.pagedResponse = {
      content: this.transactions,
      totalElements,
      totalPages,
      size,
      number: this.currentPage,
      first: this.currentPage === 0,
      last: this.currentPage >= totalPages - 1,
    };
  }

  clearFilters(): void {
    this.filter.accountId = undefined;
    this.filter.type = undefined;
    this.filter.startDate = undefined;
    this.filter.endDate = undefined;
    this.currentPage = 0;
    this.applyFilters();
  }

  loadTransactionsWithoutFilters(): void {
    this.error = "";
    this.loadTransactions();
  }

  showBackendTroubleshooting(): void {
    const msg = `Backend: ${window.location.origin}/api/admin/transactions\nFilters are now applied in the browser. If the list is empty, use "Try Loading Without Filters" to refresh data.`;
    alert(msg);
  }

  goToPage(page: number): void {
    const totalPages = this.pagedResponse?.totalPages ?? 0;
    if (page >= 0 && page < totalPages) {
      this.currentPage = page;
      this.applyFilters();
    }
  }

  getStartRecord(): number {
    if (!this.pagedResponse) return 0;
    return this.pagedResponse.number * this.pagedResponse.size + 1;
  }

  getEndRecord(): number {
    if (!this.pagedResponse) return 0;
    const start = this.getStartRecord();
    const remaining = this.pagedResponse.totalElements - (start - 1);
    return start - 1 + Math.min(this.pagedResponse.size, remaining);
  }

  getVisiblePages(): (number | string)[] {
    if (!this.pagedResponse) return [];

    const totalPages = this.pagedResponse.totalPages;
    const currentPage = this.pagedResponse.number + 1; // Convert to 1-based
    const visiblePages: (number | string)[] = [];

    if (totalPages <= 7) {
      // Show all pages if 7 or fewer
      for (let i = 1; i <= totalPages; i++) {
        visiblePages.push(i);
      }
    } else {
      // Always show first page
      visiblePages.push(1);

      if (currentPage > 4) {
        visiblePages.push("...");
      }

      // Show pages around current page
      const start = Math.max(2, currentPage - 1);
      const end = Math.min(totalPages - 1, currentPage + 1);

      for (let i = start; i <= end; i++) {
        visiblePages.push(i);
      }

      if (currentPage < totalPages - 3) {
        visiblePages.push("...");
      }

      // Always show last page
      if (totalPages > 1) {
        visiblePages.push(totalPages);
      }
    }

    return visiblePages;
  }

  changePageSize(): void {
    this.currentPage = 0;
    this.applyFilters();
  }

  jumpToPage(event: any): void {
    const target = event.target || event.currentTarget;
    const pageNumber = parseInt(
      target.value || target.previousElementSibling?.value,
      10,
    );

    if (
      pageNumber &&
      pageNumber >= 1 &&
      pageNumber <= (this.pagedResponse?.totalPages || 0)
    ) {
      this.goToPage(pageNumber - 1); // Convert to 0-based
    }
  }

  getPageNumbers(): number[] {
    if (!this.pagedResponse) return [];
    const totalPages = this.pagedResponse.totalPages;
    const currentPage = this.pagedResponse.number;
    const pages: number[] = [];

    // Show max 5 pages around current page
    const start = Math.max(0, currentPage - 2);
    const end = Math.min(totalPages - 1, currentPage + 2);

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }

    return pages;
  }

  // Banking Operations
  openOperationModal(operation: "credit" | "debit"): void {
    this.currentOperation = operation;
    this.operationForm.reset();
    this.errorMessage = "";
    this.successMessage = "";
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.operationForm.reset();
    this.errorMessage = "";
  }

  performOperation(): void {
    if (this.operationForm.invalid) return;

    this.operationLoading = true;
    this.errorMessage = "";

    const { accountId, amount, description } = this.operationForm.value;

    let operation$;
    switch (this.currentOperation) {
      case "credit":
        operation$ = this.bankingApiService.credit(
          accountId,
          amount,
          description,
        );
        break;
      case "debit":
        operation$ = this.bankingApiService.debit(
          accountId,
          amount,
          description,
        );
        break;
    }

    operation$.subscribe({
      next: () => {
        this.operationLoading = false;
        this.successMessage = `${this.getOperationTitle()} completed successfully!`;

        // Close modal
        this.closeModal();

        // Reload transactions
        this.loadTransactions();
      },
      error: (error) => {
        this.operationLoading = false;
        this.errorMessage =
          error.error?.message ||
          `${this.getOperationTitle()} failed. Please try again.`;
        console.error("Operation error:", error);
      },
    });
  }

  getOperationTitle(): string {
    switch (this.currentOperation) {
      case "credit":
        return "Credit Account";
      case "debit":
        return "Debit Account";
      default:
        return "Banking Operation";
    }
  }

  getOperationButtonClass(): string {
    switch (this.currentOperation) {
      case "credit":
        return "btn-success";
      case "debit":
        return "btn-warning";
      default:
        return "btn-primary";
    }
  }

  // Helper methods for display
  getCustomerName(transaction: any): string {
    if (transaction.customerName) return transaction.customerName;
    if (transaction.customer && transaction.customer.username) {
      return transaction.customer.username;
    }
    if (transaction.customer && transaction.customer.name) {
      return transaction.customer.name;
    }
    if (transaction.performedBy) {
      return transaction.performedBy;
    }
    return "Unknown Customer";
  }

  getAccountDisplayName(accountId: string): string {
    const account = this.accountsForSelection.find(
      (acc) => acc.accountId === accountId,
    );
    if (account) {
      return `${account.customerUsername} - ${account.customerName} (${account.accountType})`;
    }
    return `Account ${accountId}`;
  }

  getTransactionTypeBadge(type: string): string {
    switch (type?.toUpperCase()) {
      case "DEPOSIT":
      case "CREDIT":
        return "bg-success";
      case "WITHDRAWAL":
      case "DEBIT":
        return "bg-danger";
      case "TRANSFER":
        return "bg-primary";
      default:
        return "bg-secondary";
    }
  }

  getAmountClass(type: string): string {
    switch (type?.toUpperCase()) {
      case "DEPOSIT":
      case "CREDIT":
        return "text-success";
      case "WITHDRAWAL":
      case "DEBIT":
        return "text-danger";
      case "TRANSFER":
        return "text-primary";
      default:
        return "";
    }
  }
}
