import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { useAuth } from '@/context/AuthContext';
import { useCreateBooking, useGetCustomerBookings } from '@/api/bookingService/booking-controller';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Calendar, Users, Clock, History, AlertCircle } from 'lucide-react';
import { format } from 'date-fns';
import { useToast } from '@/hooks/use-toast';

const bookingSchema = z.object({
    partySize: z.number().min(1, 'At least one person is required').max(10, 'Maximum 10 people'),
    date: z.string().min(1, 'Date is required'),
    time: z.string().min(1, 'Time is required'),
});

type BookingFormValues = z.infer<typeof bookingSchema>;

export const BookingPage = () => {
    const { user } = useAuth();
    const { toast } = useToast();
    const customerId = user?.id || '';

    const { data: bookings = [], isLoading: isLoadingBookings } = useGetCustomerBookings(customerId);
    const createBooking = useCreateBooking();

    const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<BookingFormValues>({
        resolver: zodResolver(bookingSchema),
        defaultValues: {
            partySize: 2,
            date: format(new Date(), 'yyyy-MM-dd'),
            time: '19:00',
        }
    });

    const onSubmit = async (values: BookingFormValues) => {
        try {
            const bookingTime = new Date(`${values.date}T${values.time}:00`).toISOString();
            await createBooking.mutateAsync({
                customerId,
                partySize: values.partySize,
                bookingTime,
            });
            toast({
                title: "Success!",
                description: "Your table has been reserved.",
            });
            reset();
        } catch (error) {
            toast({
                title: "Error",
                description: "Failed to create booking. Please try again.",
                variant: "destructive",
            });
        }
    };

    return (
        <div className="max-w-6xl mx-auto py-8 px-4 space-y-12">
            <div className="space-y-2">
                <h1 className="text-4xl font-bold tracking-tight">Table Reservations</h1>
                <p className="text-gray-400 text-lg">Book a spot for your next pizza feast.</p>
            </div>

            <div className="grid lg:grid-cols-3 gap-8">
                {/* Booking Form */}
                <div className="lg:col-span-1">
                    <Card className="bg-[hsl(220,15%,12%)] border-gray-800 sticky top-24">
                        <CardHeader>
                            <CardTitle>Book a Table</CardTitle>
                            <CardDescription>Reserve your table in seconds.</CardDescription>
                        </CardHeader>
                        <CardContent>
                            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                                <div className="space-y-2">
                                    <Label htmlFor="partySize">Party Size</Label>
                                    <div className="relative">
                                        <Users className="absolute left-3 top-3 h-4 w-4 text-gray-500" />
                                        <Input
                                            id="partySize"
                                            type="number"
                                            className="pl-10"
                                            {...register('partySize', { valueAsNumber: true })}
                                        />
                                    </div>
                                    {errors.partySize && <p className="text-red-500 text-xs">{errors.partySize.message}</p>}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="date">Date</Label>
                                    <div className="relative">
                                        <Calendar className="absolute left-3 top-3 h-4 w-4 text-gray-500" />
                                        <Input
                                            id="date"
                                            type="date"
                                            className="pl-10"
                                            {...register('date')}
                                            min={format(new Date(), 'yyyy-MM-dd')}
                                        />
                                    </div>
                                    {errors.date && <p className="text-red-500 text-xs">{errors.date.message}</p>}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="time">Time</Label>
                                    <div className="relative">
                                        <Clock className="absolute left-3 top-3 h-4 w-4 text-gray-500" />
                                        <Input
                                            id="time"
                                            type="time"
                                            className="pl-10"
                                            {...register('time')}
                                        />
                                    </div>
                                    {errors.time && <p className="text-red-500 text-xs">{errors.time.message}</p>}
                                </div>

                                <Button 
                                    type="submit" 
                                    className="w-full bg-[hsl(var(--primary-hue),90%,55%)] hover:bg-[hsl(var(--primary-hue),90%,45%)]"
                                    disabled={isSubmitting}
                                >
                                    {isSubmitting ? 'Booking...' : 'Reserve Table'}
                                </Button>
                            </form>
                        </CardContent>
                    </Card>
                </div>

                {/* Reservation History */}
                <div className="lg:col-span-2 space-y-6">
                    <div className="flex items-center gap-2 text-xl font-semibold">
                        <History className="text-[hsl(var(--primary-hue),90%,55%)]" />
                        <h2>Your Reservations</h2>
                    </div>

                    {isLoadingBookings ? (
                        <div className="flex justify-center py-12">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                        </div>
                    ) : bookings.length === 0 ? (
                        <div className="text-center py-20 border-2 border-dashed border-gray-800 rounded-xl space-y-4">
                            <AlertCircle className="w-12 h-12 text-gray-700 mx-auto" />
                            <div className="space-y-1">
                                <p className="text-gray-400">No active reservations found.</p>
                                <p className="text-sm text-gray-600">Start by filling out the form on the left.</p>
                            </div>
                        </div>
                    ) : (
                        <div className="grid gap-4">
                            {bookings.sort((a, b) => new Date(b.bookingTime).getTime() - new Date(a.bookingTime).getTime()).map((booking) => (
                                <Card key={booking.id} className="bg-[hsl(220,15%,12%)] border-gray-800">
                                    <CardContent className="flex items-center justify-between p-6">
                                        <div className="flex items-center gap-6">
                                            <div className="w-12 h-12 rounded-full bg-gray-800 flex items-center justify-center text-[hsl(var(--primary-hue),90%,55%)]">
                                                <Calendar size={24} />
                                            </div>
                                            <div className="space-y-1">
                                                <div className="flex items-center gap-3">
                                                    <span className="font-bold text-lg">
                                                        {format(new Date(booking.bookingTime), 'EEEE, MMM dd')}
                                                    </span>
                                                    <Badge className={getStatusColor(booking.status)}>
                                                        {booking.status}
                                                    </Badge>
                                                </div>
                                                <div className="text-sm text-gray-400 flex items-center gap-4">
                                                    <span className="flex items-center gap-1">
                                                        <Clock size={14} /> {format(new Date(booking.bookingTime), 'HH:mm')}
                                                    </span>
                                                    <span className="w-1 h-1 bg-gray-600 rounded-full"></span>
                                                    <span className="flex items-center gap-1">
                                                        <Users size={14} /> Party of {booking.partySize}
                                                    </span>
                                                    <span className="w-1 h-1 bg-gray-600 rounded-full"></span>
                                                    <span>Table {booking.tableNumber}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

const getStatusColor = (status: string) => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-500/10 text-yellow-500 border-yellow-500/20';
        case 'CONFIRMED': return 'bg-green-500/10 text-green-500 border-green-500/20';
        case 'CANCELLED': return 'bg-red-500/10 text-red-500 border-red-500/20';
        case 'COMPLETED': return 'bg-blue-500/10 text-blue-500 border-blue-500/20';
        default: return 'bg-gray-500/10 text-gray-500 border-gray-500/20';
    }
};
